package com.afrouzi.dnsmaster.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.afrouzi.dnsmaster.MainActivity
import com.afrouzi.dnsmaster.R
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.VpnConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class DnsQuickTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var listeningJob: Job? = null

    companion object {
        fun updateTileState(context: Context, isConnected: Boolean = false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestListeningState(context, ComponentName(context, DnsQuickTileService::class.java))
                } catch (ignored: Exception) {}
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileUI(
            DnsRepository.connectionState.value,
            DnsRepository.connectedDns.value,
            DnsRepository.currentLanguage.value
        )
        listeningJob?.cancel()
        listeningJob = serviceScope.launch {
            combine(
                DnsRepository.connectionState,
                DnsRepository.connectedDns,
                DnsRepository.currentLanguage
            ) { state, dns, lang ->
                Triple(state, dns, lang)
            }.collect { (state, dns, lang) ->
                updateTileUI(state, dns, lang)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onClick() {
        super.onClick()
        val isConnected = DnsRepository.connectionState.value == VpnConnectionState.CONNECTED
        val isPersian = DnsRepository.currentLanguage.value == "fa"
        val tile = qsTile

        if (isConnected) {
            // Optimistic update for instant visual feedback
            tile?.let {
                it.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.subtitle = if (isPersian) "خاموش" else "Off"
                }
                it.updateTile()
            }
            val stopIntent = Intent(this, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_STOP
            }
            startService(stopIntent)
        } else {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                // Needs VPN permission dialog, launch MainActivity
                val appIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                if (Build.VERSION.SDK_INT >= 34) {
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0, appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(appIntent)
                }
            } else {
                // Optimistic update for instant visual feedback
                tile?.let {
                    it.state = Tile.STATE_ACTIVE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.subtitle = if (isPersian) "در حال اتصال..." else "Connecting..."
                    }
                    it.updateTile()
                }
                val startIntent = Intent(this, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startIntent)
                } else {
                    startService(startIntent)
                }
            }
        }
    }

    private fun updateTileUI(
        state: VpnConnectionState,
        dnsItem: DnsItem?,
        lang: String
    ) {
        val tile = qsTile ?: return
        val isConnected = state == VpnConnectionState.CONNECTED
        val isConnecting = state == VpnConnectionState.CONNECTING
        val isPersian = lang == "fa"

        tile.state = when {
            isConnected -> Tile.STATE_ACTIVE
            isConnecting -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }

        try {
            tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_dns_tile)
        } catch (ignored: Exception) {}

        tile.label = if (isConnected && dnsItem != null) dnsItem.name else if (isPersian) "دی‌ان‌اس مستر" else "DNS Master"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = when {
                isConnected -> if (isPersian) "روشن" else "Active"
                isConnecting -> if (isPersian) "در حال اتصال..." else "Connecting..."
                else -> if (isPersian) "خاموش" else "Off"
            }
        }
        tile.updateTile()
    }
}
