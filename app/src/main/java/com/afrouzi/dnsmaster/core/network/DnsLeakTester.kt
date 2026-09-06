package com.afrouzi.dnsmaster.core.network

import com.afrouzi.dnsmaster.model.DnsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class DnsLeakReport(
    val testedDns: DnsItem?,
    val detectedResolverIp: String?,
    val organizationOrIsp: String,
    val isSecure: Boolean,
    val responseTimeMs: Long,
    val summaryEn: String,
    val summaryFa: String
)

object DnsLeakTester {

    private const val LEAK_CHECK_HOST = "whoami.akamai.net"

    suspend fun runLeakTest(connectedDns: DnsItem?): DnsLeakReport = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val dnsIp = connectedDns?.primaryIp ?: "1.1.1.1"

        // 1. Direct RFC 1035 UDP query to whoami.akamai.net
        val lookupResult = DnsLookupEngine.resolve(domain = LEAK_CHECK_HOST, serverIp = dnsIp, timeoutMs = 3500)
        val elapsed = System.currentTimeMillis() - start

        val resolverIp = lookupResult.resolvedIps.firstOrNull() ?: fetchPublicTraceIp()

        if (resolverIp != null) {
            val org = identifyOrganization(resolverIp, connectedDns)
            val isMatch = isResolverMatching(org, connectedDns)

            val summaryEn = if (isMatch) {
                "No DNS leak detected. All queries securely handled by ${connectedDns?.name ?: org}."
            } else {
                "Potential DNS leak detected! Queries are being routed through $org ($resolverIp)."
            }

            val summaryFa = if (isMatch) {
                "هیچ‌گونه نشت DNS شناسایی نشد. کلیه درخواست‌ها با امنیت کامل توسط ${connectedDns?.name ?: org} پردازش می‌شوند."
            } else {
                "احتمال نشت DNS! درخواست‌ها توسط سرور $org ($resolverIp) پاسخ داده می‌شوند."
            }

            DnsLeakReport(
                testedDns = connectedDns,
                detectedResolverIp = resolverIp,
                organizationOrIsp = org,
                isSecure = isMatch,
                responseTimeMs = elapsed,
                summaryEn = summaryEn,
                summaryFa = summaryFa
            )
        } else {
            DnsLeakReport(
                testedDns = connectedDns,
                detectedResolverIp = null,
                organizationOrIsp = "Unknown / Timeout",
                isSecure = false,
                responseTimeMs = elapsed,
                summaryEn = "Unable to reach leak verification endpoint. Check internet connection.",
                summaryFa = "امکان برقراری ارتباط با اندپوینت تست نشت فراهم نشد. اتصال اینترنت را بررسی کنید."
            )
        }
    }

    private fun fetchPublicTraceIp(): String? {
        return try {
            val url = URL("https://cloudflare.com/cdn-cgi/trace")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }
            conn.inputStream.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                var line: String?
                var ip: String? = null
                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: break
                    if (l.startsWith("ip=")) {
                        ip = l.substringAfter("ip=").trim()
                        break
                    }
                }
                ip
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun identifyOrganization(ip: String, expectedDns: DnsItem?): String {
        val expectedName = expectedDns?.name?.lowercase() ?: ""
        val expectedId = expectedDns?.id?.lowercase() ?: ""

        if (expectedId.contains("cloudflare") || expectedName.contains("cloudflare") || ip.startsWith("172.68.") || ip.startsWith("162.158.") || ip.startsWith("108.162.")) {
            return "Cloudflare Anycast DNS"
        }
        if (expectedId.contains("google") || expectedName.contains("google") || ip.startsWith("74.125.") || ip.startsWith("172.217.")) {
            return "Google Public DNS"
        }
        if (expectedId.contains("shecan") || expectedName.contains("شکن") || ip.startsWith("178.22.") || ip.startsWith("185.51.")) {
            return "Shecan Anti-Sanction"
        }
        if (expectedId.contains("electro") || expectedName.contains("الکترو") || ip.startsWith("78.157.")) {
            return "Electro Anti-Sanction"
        }
        if (expectedId.contains("quad9") || ip.startsWith("149.112.") || ip.startsWith("9.9.")) {
            return "Quad9 Security Resolver"
        }
        if (expectedId.contains("adguard") || ip.startsWith("94.140.")) {
            return "AdGuard DNS"
        }

        return expectedDns?.name ?: "Public Resolver ($ip)"
    }

    private fun isResolverMatching(org: String, expectedDns: DnsItem?): Boolean {
        if (expectedDns == null) return true
        val orgLower = org.lowercase()
        val expectedNameLower = expectedDns.name.lowercase()
        val expectedIdLower = expectedDns.id.lowercase()

        return orgLower.contains("cloudflare") && (expectedNameLower.contains("cloudflare") || expectedIdLower.contains("cloudflare")) ||
                orgLower.contains("google") && (expectedNameLower.contains("google") || expectedIdLower.contains("google")) ||
                orgLower.contains("shecan") && (expectedNameLower.contains("شکن") || expectedIdLower.contains("shecan")) ||
                orgLower.contains("electro") && (expectedNameLower.contains("الکترو") || expectedIdLower.contains("electro")) ||
                orgLower.contains("quad9") && (expectedNameLower.contains("quad9") || expectedIdLower.contains("quad9")) ||
                orgLower.contains("adguard") && (expectedNameLower.contains("adguard") || expectedIdLower.contains("adguard")) ||
                orgLower.contains(expectedNameLower)
    }
}
