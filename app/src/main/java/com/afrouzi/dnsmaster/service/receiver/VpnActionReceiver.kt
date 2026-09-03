package com.afrouzi.dnsmaster.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afrouzi.dnsmaster.service.DnsVpnService

class VpnActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISCONNECT = "com.afrouzi.dnsmaster.ACTION_DISCONNECT"
        const val ACTION_CONNECT = "com.afrouzi.dnsmaster.ACTION_CONNECT"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        android.util.Log.i("VpnActionReceiver", "Received action: ${intent?.action}")
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                val stopIntent = Intent(context, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_STOP
                }
                context.startService(stopIntent)
            }
            ACTION_CONNECT -> {
                val startIntent = Intent(context, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                }
                context.startService(startIntent)
            }
        }
    }
}
