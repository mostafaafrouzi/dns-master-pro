package com.afrouzi.dnsmaster.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.NetworkInterface

data class NetworkInfoState(
    val transportType: NetworkTransport = NetworkTransport.NONE,
    val transportNameEn: String = "Disconnected",
    val transportNameFa: String = "قطع شده",
    val isInternetValidated: Boolean = false,
    val hasIpv4: Boolean = false,
    val hasIpv6: Boolean = false,
    val localIpAddress: String? = null
)

enum class NetworkTransport {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    NONE
}

object NetworkDiagnosticsHelper {

    fun getNetworkInfo(context: Context): NetworkInfoState {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkInfoState()

        val activeNetwork = cm.activeNetwork ?: return NetworkInfoState()
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkInfoState()

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransport.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransport.CELLULAR
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkTransport.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkTransport.VPN
            else -> NetworkTransport.NONE
        }

        val nameEn = when (transport) {
            NetworkTransport.WIFI -> "Wi-Fi Network"
            NetworkTransport.CELLULAR -> "Mobile Cellular Data"
            NetworkTransport.ETHERNET -> "Ethernet LAN"
            NetworkTransport.VPN -> "Encrypted Tunnel"
            NetworkTransport.NONE -> "No Connection"
        }

        val nameFa = when (transport) {
            NetworkTransport.WIFI -> "شبکه وای‌فای (Wi-Fi)"
            NetworkTransport.CELLULAR -> "دیتای همراه (سیم‌کارت)"
            NetworkTransport.ETHERNET -> "اتصال کابلی (LAN)"
            NetworkTransport.VPN -> "تونل ایمن"
            NetworkTransport.NONE -> "اینترنت متصل نیست"
        }

        val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        var hasV4 = false
        var hasV6 = false
        var localIp: String? = null

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return NetworkInfoState(
                transportType = transport,
                transportNameEn = nameEn,
                transportNameFa = nameFa,
                isInternetValidated = isValidated
            )

            for (nif in interfaces) {
                if (nif.isLoopback || !nif.isUp) continue
                for (addr in nif.inetAddresses) {
                    if (addr.isLoopbackAddress) continue
                    if (addr is Inet4Address && !addr.isLinkLocalAddress) {
                        hasV4 = true
                        if (localIp == null) localIp = addr.hostAddress
                    } else if (addr is Inet6Address && !addr.isLinkLocalAddress) {
                        hasV6 = true
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore socket/permission exception
        }

        return NetworkInfoState(
            transportType = transport,
            transportNameEn = nameEn,
            transportNameFa = nameFa,
            isInternetValidated = isValidated,
            hasIpv4 = hasV4,
            hasIpv6 = hasV6,
            localIpAddress = localIp
        )
    }
}
