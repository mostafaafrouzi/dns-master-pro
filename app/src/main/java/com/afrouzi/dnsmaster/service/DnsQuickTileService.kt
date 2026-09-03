package com.afrouzi.dnsmaster.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.afrouzi.dnsmaster.MainActivity
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.model.VpnConnectionState

@RequiresApi(Build.VERSION_CODES.N)
class DnsQuickTileService : TileService() {

    companion object {
        fun updateTileState(context: Context, isConnected: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestListeningState(context, ComponentName(context, DnsQuickTileService::class.java))
                } catch (ignored: Exception) {}
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileUI()
    }

    override fun onClick() {
        super.onClick()
        val isConnected = DnsRepository.connectionState.value == VpnConnectionState.CONNECTED

        if (isConnected) {
            val stopIntent = Intent(this, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_STOP
            }
            startService(stopIntent)
        } else {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                // Needs VPN permission, open main activity
                val appIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivityAndCollapse(appIntent)
            } else {
                // VPN permission already granted, start immediately
                val startIntent = Intent(this, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                }
                startService(startIntent)
            }
        }
        updateTileUI()
    }

    private fun updateTileUI() {
        val tile = qsTile ?: return
        val isConnected = DnsRepository.connectionState.value == VpnConnectionState.CONNECTED
        val dnsItem = DnsRepository.connectedDns.value

        tile.state = if (isConnected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isConnected && dnsItem != null) dnsItem.name else "DNS Master"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isConnected) "Active" else "Off"
        }
        tile.updateTile()
    }
}
