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

object NotificationHelper {

    const val CHANNEL_ID = "dns_master_vpn_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "DNS Master Service"
            val channelDescription = "Shows active DNS status, duration and quick controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
                setShowBadge(false)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun buildVpnNotification(
        context: Context,
        dnsItem: DnsItem?,
        isPersian: Boolean = false,
        startTime: Long = 0L,
        isConnected: Boolean = true
    ): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(context, DnsVpnService::class.java).apply {
            action = DnsVpnService.ACTION_STOP
        }
        val disconnectPendingIntent = PendingIntent.getService(
            context, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dnsName = dnsItem?.name ?: if (isPersian) "سرور فعال" else "Active DNS"
        val ips = if (dnsItem != null) {
            if (dnsItem.secondaryIp.isNotBlank()) "${dnsItem.primaryIp} | ${dnsItem.secondaryIp}" else dnsItem.primaryIp
        } else {
            "10.0.0.2"
        }

        val title = if (isConnected) {
            if (isPersian) "متصل به: $dnsName" else "Connected to: $dnsName"
        } else {
            if (isPersian) "در حال اتصال به: $dnsName" else "Connecting to: $dnsName"
        }

        val content = if (isPersian) "آدرس سرور: $ips" else "Server IP: $ips"
        val subText = if (isConnected) {
            if (isPersian) "وضعیت: متصل" else "Status: Connected"
        } else {
            if (isPersian) "وضعیت: در حال برقراری" else "Status: Connecting"
        }
        val disconnectLabel = if (isPersian) "خاموش کردن" else "Disconnect"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(subText)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                disconnectLabel,
                disconnectPendingIntent
            )

        if (isConnected && startTime > 0) {
            builder.setUsesChronometer(true)
            builder.setWhen(startTime)
            builder.setShowWhen(true)
        } else {
            builder.setUsesChronometer(false)
            builder.setShowWhen(false)
        }

        return builder.build()
    }
}
