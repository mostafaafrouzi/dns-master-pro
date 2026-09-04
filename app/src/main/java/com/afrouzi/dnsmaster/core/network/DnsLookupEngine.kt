package com.afrouzi.dnsmaster.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Random

data class DnsLookupResult(
    val domain: String,
    val serverIp: String,
    val isSuccess: Boolean,
    val rCode: String,
    val resolvedIps: List<String>,
    val ttlSeconds: Long?,
    val latencyMs: Long,
    val isAntiSanctionVerified: Boolean = false,
    val errorMessage: String? = null
)

object DnsLookupEngine {

    private const val DNS_PORT = 53
    private const val DEFAULT_TIMEOUT_MS = 3500

    /**
     * Performs a real UDP DNS query (RFC 1035) for domain A record against the specified DNS server.
     */
    suspend fun resolve(
        domain: String,
        serverIp: String,
        timeoutMs: Int = DEFAULT_TIMEOUT_MS
    ): DnsLookupResult = withContext(Dispatchers.IO) {
        val cleanDomain = domain.trim().trimEnd('.')
        if (cleanDomain.isBlank()) {
            return@withContext DnsLookupResult(
                domain = domain,
                serverIp = serverIp,
                isSuccess = false,
                rCode = "INVALID_DOMAIN",
                resolvedIps = emptyList(),
                ttlSeconds = null,
                latencyMs = 0L,
                errorMessage = "Invalid domain name"
            )
        }

        var socket: DatagramSocket? = null
        try {
            val queryId = Random().nextInt(65535)
            val queryBytes = buildDnsQuery(cleanDomain, queryId)

            val serverAddress = InetAddress.getByName(serverIp)
            socket = DatagramSocket().apply {
                soTimeout = timeoutMs
            }

            val sendPacket = DatagramPacket(queryBytes, queryBytes.size, serverAddress, DNS_PORT)
            val startTime = System.nanoTime()
            socket.send(sendPacket)

            val receiveBuffer = ByteArray(1024)
            val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
            socket.receive(receivePacket)
            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000L

            val responseBytes = receivePacket.data.copyOf(receivePacket.length)
            parseDnsResponse(cleanDomain, serverIp, queryId, responseBytes, elapsedMs)
        } catch (e: Exception) {
            val isTimeout = e is java.net.SocketTimeoutException
            DnsLookupResult(
                domain = cleanDomain,
                serverIp = serverIp,
                isSuccess = false,
                rCode = if (isTimeout) "TIMEOUT" else "ERROR",
                resolvedIps = emptyList(),
                ttlSeconds = null,
                latencyMs = if (isTimeout) timeoutMs.toLong() else 0L,
                errorMessage = e.message ?: "Resolution failed"
            )
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    private fun buildDnsQuery(domain: String, queryId: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        // Header (12 bytes)
        dos.writeShort(queryId) // Transaction ID
        dos.writeShort(0x0100)  // Flags: Standard query, Recursion Desired (RD = 1)
        dos.writeShort(1)       // QDCOUNT: 1 question
        dos.writeShort(0)       // ANCOUNT: 0
        dos.writeShort(0)       // NSCOUNT: 0
        dos.writeShort(0)       // ARCOUNT: 0

        // Question Section
        val labels = domain.split('.')
        for (label in labels) {
            val bytes = label.toByteArray(Charsets.US_ASCII)
            dos.writeByte(bytes.size)
            dos.write(bytes)
        }
        dos.writeByte(0) // Null byte for root

        dos.writeShort(1) // QTYPE: 1 (A record)
        dos.writeShort(1) // QCLASS: 1 (IN Internet)

        dos.flush()
        return baos.toByteArray()
    }

    private fun parseDnsResponse(
        domain: String,
        serverIp: String,
        expectedId: Int,
        data: ByteArray,
        latencyMs: Long
    ): DnsLookupResult {
        if (data.size < 12) {
            return DnsLookupResult(
                domain = domain,
                serverIp = serverIp,
                isSuccess = false,
                rCode = "MALFORMED",
                resolvedIps = emptyList(),
                ttlSeconds = null,
                latencyMs = latencyMs,
                errorMessage = "Response packet too short"
            )
        }

        val id = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
        val flags = ((data[2].toInt() and 0xFF) shl 8) or (data[3].toInt() and 0xFF)
        val rcodeBits = flags and 0x0F

        val rcodeName = when (rcodeBits) {
            0 -> "NOERROR"
            1 -> "FORMERR"
            2 -> "SERVFAIL"
            3 -> "NXDOMAIN"
            4 -> "NOTIMP"
            5 -> "REFUSED"
            else -> "RCODE_$rcodeBits"
        }

        if (rcodeBits != 0) {
            return DnsLookupResult(
                domain = domain,
                serverIp = serverIp,
                isSuccess = false,
                rCode = rcodeName,
                resolvedIps = emptyList(),
                ttlSeconds = null,
                latencyMs = latencyMs,
                errorMessage = "Server returned $rcodeName"
            )
        }

        val qdcount = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
        val ancount = ((data[6].toInt() and 0xFF) shl 8) or (data[7].toInt() and 0xFF)

        var offset = 12

        // Skip Question section
        for (i in 0 until qdcount) {
            offset = skipName(data, offset)
            offset += 4 // QTYPE (2) + QCLASS (2)
            if (offset > data.size) break
        }

        // Parse Answers
        val ips = mutableListOf<String>()
        var minTtl: Long? = null

        for (i in 0 until ancount) {
            if (offset >= data.size) break
            offset = skipName(data, offset)
            if (offset + 10 > data.size) break

            val type = ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
            // class at offset + 2
            val ttl = ((data[offset + 4].toLong() and 0xFF) shl 24) or
                    ((data[offset + 5].toLong() and 0xFF) shl 16) or
                    ((data[offset + 6].toLong() and 0xFF) shl 8) or
                    (data[offset + 7].toLong() and 0xFF)
            val rdLength = ((data[offset + 8].toInt() and 0xFF) shl 8) or (data[offset + 9].toInt() and 0xFF)
            offset += 10

            if (offset + rdLength > data.size) break

            if (type == 1 && rdLength == 4) { // IPv4 A Record
                val ip = "${data[offset].toInt() and 0xFF}.${data[offset + 1].toInt() and 0xFF}.${data[offset + 2].toInt() and 0xFF}.${data[offset + 3].toInt() and 0xFF}"
                ips.add(ip)
                if (minTtl == null || ttl < minTtl) {
                    minTtl = ttl
                }
            }
            offset += rdLength
        }

        val isAntiSanction = checkIfAntiSanction(domain, ips)

        return DnsLookupResult(
            domain = domain,
            serverIp = serverIp,
            isSuccess = ips.isNotEmpty(),
            rCode = rcodeName,
            resolvedIps = ips,
            ttlSeconds = minTtl,
            latencyMs = latencyMs,
            isAntiSanctionVerified = isAntiSanction
        )
    }

    private fun skipName(data: ByteArray, startOffset: Int): Int {
        var offset = startOffset
        while (offset < data.size) {
            val len = data[offset].toInt() and 0xFF
            if (len == 0) {
                return offset + 1
            }
            if ((len and 0xC0) == 0xC0) {
                // Compression pointer (2 bytes)
                return offset + 2
            }
            offset += 1 + len
        }
        return offset
    }

    private fun checkIfAntiSanction(domain: String, ips: List<String>): Boolean {
        val sanctionedDomains = listOf("docker.com", "developer.android.com", "cloud.google.com", "shecan.ir", "openai.com", "oracle.com")
        val isSanctioned = sanctionedDomains.any { domain.contains(it, ignoreCase = true) }
        if (!isSanctioned) return false

        // Check if any resolved IP belongs to known Iranian reverse-proxy subnets used by Shecan/403/Begzar
        for (ip in ips) {
            if (ip.startsWith("178.22.122.") || ip.startsWith("185.51.200.") ||
                ip.startsWith("10.202.") || ip.startsWith("216.239.38.")
            ) {
                return true
            }
        }
        return false
    }
}
