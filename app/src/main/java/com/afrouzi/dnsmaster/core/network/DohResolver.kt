package com.afrouzi.dnsmaster.core.network

import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object DohResolver {

    private const val TAG = "DohResolver"
    private const val TIMEOUT_MS = 3500

    /**
     * Resolves a DNS query using DNS over HTTPS (RFC 8484).
     * @param dohUrl Endpoint URL (e.g. https://cloudflare-dns.com/dns-query)
     * @param dnsQueryBytes Standard RFC 1035 wire format DNS query
     * @return Raw RFC 1035 DNS response bytes, or null on error
     */
    fun resolve(dohUrl: String, dnsQueryBytes: ByteArray): ByteArray? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(dohUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                setRequestProperty("Content-Type", "application/dns-message")
                setRequestProperty("Accept", "application/dns-message")
                setRequestProperty("User-Agent", "DNSMaster/1.3")
                setFixedLengthStreamingMode(dnsQueryBytes.size)
            }

            // Send DNS wire format query
            connection.outputStream.use { os ->
                os.write(dnsQueryBytes)
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream: InputStream = connection.inputStream
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(2048)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    baos.write(buffer, 0, bytesRead)
                }
                val responseBytes = baos.toByteArray()
                if (responseBytes.size >= 12) {
                    responseBytes
                } else {
                    Log.w(TAG, "DoH response too short: ${responseBytes.size} bytes")
                    null
                }
            } else {
                Log.w(TAG, "DoH server responded with HTTP $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "DoH resolution error for $dohUrl: ${e.message}")
            null
        } finally {
            try {
                connection?.disconnect()
            } catch (ignored: Exception) {}
        }
    }
}
