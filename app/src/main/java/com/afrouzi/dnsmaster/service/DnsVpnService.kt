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

            // Disallow our own app so speed tests and internal sockets bypass the VPN
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not disallow package $packageName", e)
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
                secondaryDnsIp = dnsItem.secondaryIp.trim()
            )
            packetForwarder = forwarder

            val thread = Thread(forwarder, "DnsForwarderThread")
            forwarderThread = thread
            thread.start()

            DnsRepository.connectionState.value = VpnConnectionState.CONNECTED
            DnsRepository.connectedDns.value = dnsItem
            DnsRepository.connectedStartTime.value = System.currentTimeMillis()

            DnsQuickTileService.updateTileState(this, true)
            Log.i(TAG, "DNS VPN started successfully with ${dnsItem.name} (${dnsItem.primaryIp})")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting DNS VPN", e)
            stopVpn()
        }
    }

    private fun cleanupForwarder() {
        try {
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
