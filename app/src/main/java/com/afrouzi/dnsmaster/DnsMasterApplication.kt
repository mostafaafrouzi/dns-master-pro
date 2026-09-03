package com.afrouzi.dnsmaster

import android.app.Application
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.service.NotificationHelper

class DnsMasterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        DnsRepository.getInstance(this)
    }
}
