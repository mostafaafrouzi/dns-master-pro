package com.afrouzi.dnsmaster.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.afrouzi.dnsmaster.MainActivity
import com.afrouzi.dnsmaster.R
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.service.receiver.VpnActionReceiver

object NotificationHelper {

    const val CHANNEL_ID = "dns_master_vpn_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "DNS Master Service"
            val channelDescription = "Shows active DNS status and quick control buttons"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun buildVpnNotification(context: Context, dnsItem: DnsItem?, isPersian: Boolean = false): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(context, VpnActionReceiver::class.java).apply {
            action = VpnActionReceiver.ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getBroadcast(
            context, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dnsName = dnsItem?.name ?: if (isPersian) "سرور فعال" else "Active DNS"
        val ips = if (dnsItem != null) {
            if (dnsItem.secondaryIp.isNotBlank()) "${dnsItem.primaryIp} | ${dnsItem.secondaryIp}" else dnsItem.primaryIp
        } else {
            "10.0.0.2"
        }

        val title = if (isPersian) "اتصال فعال: $dnsName" else "Connected: $dnsName"
        val content = if (isPersian) "آدرس سرور: $ips" else "Server IP: $ips"
        val disconnectLabel = if (isPersian) "قطع اتصال" else "Disconnect"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                disconnectLabel,
                disconnectPendingIntent
            )
            .build()
    }
}
