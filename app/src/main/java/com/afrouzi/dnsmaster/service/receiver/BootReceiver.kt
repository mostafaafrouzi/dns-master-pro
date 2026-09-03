package com.afrouzi.dnsmaster.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.service.DnsVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = DnsRepository.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                val shouldAutoConnect = repository.autoConnectBootFlow.first()
                if (shouldAutoConnect) {
                    val vpnIntent = Intent(context, DnsVpnService::class.java).apply {
                        action = DnsVpnService.ACTION_START
                    }
                    context.startService(vpnIntent)
                }
            }
        }
    }
}
