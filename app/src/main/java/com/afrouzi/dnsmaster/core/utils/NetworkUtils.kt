package com.afrouzi.dnsmaster.core.utils

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

object NetworkUtils {

    fun isValidIpv4(ip: String): Boolean {
        if (ip.isBlank()) return false
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            val num = part.toIntOrNull()
            num != null && num in 0..255 && (part == "0" || !part.startsWith("0"))
        }
    }

    fun isValidIpv6(ip: String): Boolean {
        if (ip.isBlank()) return false
        return try {
            val addr = InetAddress.getByName(ip)
            addr is Inet6Address
        } catch (e: Exception) {
            false
        }
    }

    fun isValidIp(ip: String): Boolean {
        return isValidIpv4(ip) || isValidIpv6(ip)
    }

    fun formatPing(pingMs: Long?): String {
        return if (pingMs != null && pingMs >= 0) "${pingMs}ms" else "--"
    }
}
