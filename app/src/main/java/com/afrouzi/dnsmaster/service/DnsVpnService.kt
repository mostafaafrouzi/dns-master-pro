package com.afrouzi.dnsmaster.service

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.afrouzi.dnsmaster.data.DefaultDnsServers
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.VpnConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream

class DnsVpnService : VpnService() {

    companion object {
        const val TAG = "DnsVpnService"
        const val ACTION_START = "com.afrouzi.dnsmaster.ACTION_START"
        const val ACTION_STOP = "com.afrouzi.dnsmaster.ACTION_STOP"
        const val ACTION_UPDATE = "com.afrouzi.dnsmaster.ACTION_UPDATE"
        const val EXTRA_DNS_ID = "com.afrouzi.dnsmaster.EXTRA_DNS_ID"

        // RFC 5737 Test-Net address that will not collide with any local LAN/Wi-Fi router
        const val TUN_INTERFACE_IP = "192.0.2.1"
        const val TUN_PREFIX_LENGTH = 24
        // Virtual DNS IP: inside the TUN /24 subnet so it is automatically routed
        // through tun0 without needing explicit /32 host routes for external IPs.
        const val VIRTUAL_DNS_IP = "192.0.2.53"
    }

    private fun isValidIpv4(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnInput: FileInputStream? = null
    private var vpnOutput: FileOutputStream? = null
    private var packetForwarder: DnsPacketForwarder? = null
    private var forwarderThread: Thread? = null
    private var autoDisconnectJob: kotlinx.coroutines.Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: DnsRepository

    override fun onCreate() {
        super.onCreate()
        repository = DnsRepository.getInstance(this)
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val dnsId = intent.getStringExtra(EXTRA_DNS_ID)
                serviceScope.launch { startVpn(dnsId) }
            }
            ACTION_STOP -> {
                serviceScope.launch { stopVpn() }
            }
            ACTION_UPDATE -> {
                val dnsId = intent.getStringExtra(EXTRA_DNS_ID)
                serviceScope.launch {
                    if (DnsRepository.connectionState.value == VpnConnectionState.CONNECTED) {
                        startVpn(dnsId)
                    }
                }
            }
        }
        return START_STICKY
    }

    private suspend fun startVpn(specificDnsId: String?) {
        try {
            DnsRepository.connectionState.value = VpnConnectionState.CONNECTING

            val customList = repository.customDnsListFlow.first()
            val dnsId = specificDnsId ?: repository.selectedDnsIdFlow.first()
            val dnsItem = repository.getDnsById(dnsId, customList) ?: DefaultDnsServers.list.first()

            val dohEnabled = repository.dohEnabledFlow.first()
            val localCacheEnabled = repository.localCacheEnabledFlow.first()
            val splitMode = repository.splitTunnelModeFlow.first()
            val splitPackages = repository.splitTunnelPackagesFlow.first()
            val autoDisconnectMinutes = repository.autoDisconnectMinutesFlow.first()

            val isPersian = repository.languageFlow.first() == "fa"
            val notification = NotificationHelper.buildVpnNotification(this, dnsItem, isPersian)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    try {
                        startForeground(
                            NotificationHelper.NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                        )
                    } catch (e: Exception) {
                        startForeground(NotificationHelper.NOTIFICATION_ID, notification)
                    }
                } else {
                    startForeground(NotificationHelper.NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID, notification)
            }

            cleanupForwarder()

            val builder = Builder()
                .setSession("DNS Master - ${dnsItem.name}")
                .setMtu(1500)
                .addAddress(TUN_INTERFACE_IP, 32)
                .addDnsServer(VIRTUAL_DNS_IP)
                .addRoute(VIRTUAL_DNS_IP, 32)

            // Apply Split Tunneling configurations
            when (splitMode) {
                com.afrouzi.dnsmaster.model.SplitTunnelMode.BYPASS_SELECTED -> {
                    try { builder.addDisallowedApplication(packageName) } catch (e: Exception) {}
                    for (pkg in splitPackages) {
                        if (pkg != packageName) {
                            try {
                                builder.addDisallowedApplication(pkg)
                            } catch (e: Exception) {
                                Log.w(TAG, "Could not disallow package $pkg", e)
                            }
                        }
                    }
                }
                com.afrouzi.dnsmaster.model.SplitTunnelMode.ONLY_SELECTED -> {
                    for (pkg in splitPackages) {
                        if (pkg != packageName) {
                            try {
                                builder.addAllowedApplication(pkg)
                            } catch (e: Exception) {
                                Log.w(TAG, "Could not allow package $pkg", e)
                            }
                        }
                    }
                }
                com.afrouzi.dnsmaster.model.SplitTunnelMode.ALL_APPS -> {
                    try { builder.addDisallowedApplication(packageName) } catch (e: Exception) {}
                }
            }

            builder.allowBypass()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                builder.setUnderlyingNetworks(null)
            }

            vpnInterface = builder.establish()

            val pfd = vpnInterface
            if (pfd == null) {
                Log.e(TAG, "Failed to establish VPN interface (null)")
                stopVpn()
                return
            }

            val inStream = FileInputStream(pfd.fileDescriptor)
            val outStream = FileOutputStream(pfd.fileDescriptor)
            vpnInput = inStream
            vpnOutput = outStream

            val forwarder = DnsPacketForwarder(
                vpnService = this,
                vpnInput = inStream,
                vpnOutput = outStream,
                primaryDnsIp = dnsItem.primaryIp.trim(),
                secondaryDnsIp = dnsItem.secondaryIp.trim(),
                dohUrl = dnsItem.dohUrl,
                isDohEnabled = dohEnabled,
                isLocalCacheEnabled = localCacheEnabled,
                onCacheHit = {
                    DnsRepository.cacheHitsCount.value++
                }
            )
            packetForwarder = forwarder

            val thread = Thread(forwarder, "DnsForwarderThread")
            forwarderThread = thread
            thread.start()

            val startTime = System.currentTimeMillis()
            DnsRepository.connectionState.value = VpnConnectionState.CONNECTED
            DnsRepository.connectedDns.value = dnsItem
            DnsRepository.connectedStartTime.value = startTime
            DnsRepository.isDohActive.value = dohEnabled && !dnsItem.dohUrl.isNullOrBlank()

            // Setup Auto Disconnect Timer if configured
            autoDisconnectJob?.cancel()
            if (autoDisconnectMinutes > 0) {
                autoDisconnectJob = serviceScope.launch {
                    var remaining = autoDisconnectMinutes * 60L
                    while (remaining > 0) {
                        DnsRepository.autoDisconnectRemainingSeconds.value = remaining
                        kotlinx.coroutines.delay(1000)
                        remaining--
                    }
                    DnsRepository.autoDisconnectRemainingSeconds.value = null
                    Log.i(TAG, "Auto-disconnect timer expired. Shutting down VPN.")
                    stopVpn()
                }
            } else {
                DnsRepository.autoDisconnectRemainingSeconds.value = null
            }

            // Update notification to Connected state with live Chronometer (v2rayNG style)
            try {
                val connectedNotification = NotificationHelper.buildVpnNotification(
                    context = this,
                    dnsItem = dnsItem,
                    isPersian = isPersian,
                    startTime = startTime,
                    isConnected = true
                )
                val notifManager = getSystemService(NotificationManager::class.java)
                notifManager?.notify(NotificationHelper.NOTIFICATION_ID, connectedNotification)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update notification with chronometer", e)
            }

            DnsQuickTileService.updateTileState(this, true)
            Log.i(TAG, "DNS VPN started successfully with ${dnsItem.name} (${dnsItem.primaryIp}) [DoH: ${DnsRepository.isDohActive.value}]")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting DNS VPN", e)
            stopVpn()
        }
    }

    private fun cleanupForwarder() {
        try {
            autoDisconnectJob?.cancel()
            autoDisconnectJob = null
            DnsRepository.autoDisconnectRemainingSeconds.value = null
            DnsRepository.isDohActive.value = false
            DnsRepository.cacheHitsCount.value = 0L

            packetForwarder?.stop()
            packetForwarder = null
            forwarderThread?.interrupt()
            forwarderThread = null

            try { vpnInput?.close() } catch (e: Exception) {}
            vpnInput = null
            try { vpnOutput?.close() } catch (e: Exception) {}
            vpnOutput = null
            try { vpnInterface?.close() } catch (e: Exception) {}
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    private fun stopVpn() {
        DnsRepository.connectionState.value = VpnConnectionState.DISCONNECTING
        cleanupForwarder()

        DnsRepository.connectionState.value = VpnConnectionState.DISCONNECTED
        DnsRepository.connectedDns.value = null
        DnsRepository.connectedStartTime.value = 0L
        DnsRepository.autoDisconnectRemainingSeconds.value = null
        DnsRepository.isDohActive.value = false

        DnsQuickTileService.updateTileState(this, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        try {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(NotificationHelper.NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification", e)
        }
        stopSelf()
    }


    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        cleanupForwarder()
        serviceScope.cancel()
        super.onDestroy()
    }
}
