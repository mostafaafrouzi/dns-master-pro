package com.afrouzi.dnsmaster.data.speedtest

import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.SpeedTestResult
import com.afrouzi.dnsmaster.model.SpeedTestStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.random.Random

object DnsSpeedTester {

    private const val QUERY_TIMEOUT_MS = 2500
    private val TEST_DOMAINS = listOf("google.com", "cloudflare.com", "wikipedia.org")

    /**
     * Builds a standard raw RFC 1035 DNS Query packet for type A record.
     */
    private fun buildDnsQuery(domain: String): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        // 1. Transaction ID (random 16-bit)
        val transactionId = Random.nextInt(0x0001, 0xFFFF)
        dos.writeShort(transactionId)

        // 2. Flags: Standard query, Recursion Desired (0x0100)
        dos.writeShort(0x0100)

        // 3. Questions count = 1
        dos.writeShort(0x0001)

        // 4. Answer RRs = 0, Authority RRs = 0, Additional RRs = 0
        dos.writeShort(0x0000)
        dos.writeShort(0x0000)
        dos.writeShort(0x0000)

        // 5. Query Name (encoded labels: 6"google"3"com"0)
        for (label in domain.split(".")) {
            val bytes = label.toByteArray(Charsets.US_ASCII)
            dos.writeByte(bytes.size)
            dos.write(bytes)
        }
        dos.writeByte(0x00) // End of labels

        // 6. Type = A (1)
        dos.writeShort(0x0001)

        // 7. Class = IN (1)
        dos.writeShort(0x0001)

        dos.flush()
        return baos.toByteArray()
    }

    data class PingDetailedStats(
        val avgPingMs: Long,
        val packetLossPercent: Int,
        val jitterMs: Long
    )

    /**
     * Measures round trip time for a DNS query in milliseconds.
     */
    suspend fun pingDnsServer(ip: String, domain: String = "google.com", timeoutMs: Int = QUERY_TIMEOUT_MS): Long? {
        return withContext(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                val queryBytes = buildDnsQuery(domain)
                val targetAddr = InetAddress.getByName(ip)
                val packet = DatagramPacket(queryBytes, queryBytes.size, targetAddr, 53)

                socket = DatagramSocket()
                socket.soTimeout = timeoutMs

                val startTime = System.currentTimeMillis()
                socket.send(packet)

                val buffer = ByteArray(512)
                val receivePacket = DatagramPacket(buffer, buffer.size)
                socket.receive(receivePacket)
                val rtt = System.currentTimeMillis() - startTime

                if (receivePacket.length > 12) rtt else null
            } catch (e: Exception) {
                null
            } finally {
                try { socket?.close() } catch (ignored: Exception) {}
            }
        }
    }

    /**
     * Sends multiple DNS packets to accurately calculate Average Ping, Packet Loss %, and Jitter.
     */
    suspend fun pingDnsServerDetailed(
        ip: String,
        domain: String = "google.com",
        packetCount: Int = 4,
        timeoutPerPacketMs: Int = 1800
    ): PingDetailedStats? {
        return withContext(Dispatchers.IO) {
            val successfulRtts = mutableListOf<Long>()
            val targetAddr = try {
                InetAddress.getByName(ip)
            } catch (e: Exception) {
                return@withContext null
            }

            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                socket.soTimeout = timeoutPerPacketMs

                for (i in 0 until packetCount) {
                    try {
                        val queryBytes = buildDnsQuery(domain)
                        val packet = DatagramPacket(queryBytes, queryBytes.size, targetAddr, 53)
                        val startTime = System.currentTimeMillis()
                        socket.send(packet)

                        val buffer = ByteArray(512)
                        val receivePacket = DatagramPacket(buffer, buffer.size)
                        socket.receive(receivePacket)
                        val rtt = System.currentTimeMillis() - startTime
                        if (receivePacket.length > 12) {
                            successfulRtts.add(rtt)
                        }
                    } catch (ignored: Exception) {
                        // Packet loss or timeout
                    }
                    if (i < packetCount - 1) {
                        kotlinx.coroutines.delay(20)
                    }
                }
            } finally {
                try { socket?.close() } catch (ignored: Exception) {}
            }

            val receivedCount = successfulRtts.size
            val lossPercent = ((packetCount - receivedCount) * 100) / packetCount

            if (receivedCount > 0) {
                val avgPing = successfulRtts.average().toLong()
                val jitter = if (receivedCount > 1) {
                    successfulRtts.map { kotlin.math.abs(it - avgPing) }.average().toLong()
                } else {
                    0L
                }
                PingDetailedStats(
                    avgPingMs = avgPing,
                    packetLossPercent = lossPercent,
                    jitterMs = jitter
                )
            } else {
                null
            }
        }
    }

    /**
     * Tests a single DnsItem, calculating Ping, Packet Loss, and Jitter.
     */
    suspend fun testDnsItem(item: DnsItem): SpeedTestResult {
        return withContext(Dispatchers.IO) {
            val primaryStats = pingDnsServerDetailed(item.primaryIp)
            val finalStats = if (primaryStats != null) {
                primaryStats
            } else if (item.secondaryIp.isNotBlank()) {
                pingDnsServerDetailed(item.secondaryIp)
            } else {
                null
            }

            if (finalStats != null) {
                SpeedTestResult(
                    dnsItem = item.copy(pingMs = finalStats.avgPingMs),
                    pingMs = finalStats.avgPingMs,
                    packetLoss = finalStats.packetLossPercent,
                    jitterMs = finalStats.jitterMs,
                    status = SpeedTestStatus.SUCCESS
                )
            } else {
                SpeedTestResult(
                    dnsItem = item,
                    pingMs = null,
                    packetLoss = 100,
                    jitterMs = null,
                    status = SpeedTestStatus.TIMEOUT
                )
            }
        }
    }

    /**
     * Concurrently benchmarks a list of DNS servers, emitting progressive updates.
     */
    fun benchmarkAll(items: List<DnsItem>): Flow<List<SpeedTestResult>> = flow {
        val resultsMap = items.associate { it.id to SpeedTestResult(it, null, SpeedTestStatus.TESTING) }.toMutableMap()
        emit(resultsMap.values.toList())

        // Run chunks of 6 concurrent queries to avoid socket exhaustion
        val chunkSize = 6
        val chunks = items.chunked(chunkSize)

        for (chunk in chunks) {
            val deferreds = withContext(Dispatchers.IO) {
                chunk.map { item ->
                    async {
                        val result = testDnsItem(item)
                        result
                    }
                }
            }

            val chunkResults = deferreds.awaitAll()
            for (res in chunkResults) {
                resultsMap[res.dnsItem.id] = res
            }

            // Identify fastest
            val minSuccess = resultsMap.values
                .filter { it.status == SpeedTestStatus.SUCCESS && it.pingMs != null }
                .minByOrNull { it.pingMs ?: Long.MAX_VALUE }

            val updatedList = resultsMap.values.map { r ->
                if (minSuccess != null && r.dnsItem.id == minSuccess.dnsItem.id) {
                    r.copy(isFastest = true)
                } else {
                    r.copy(isFastest = false)
                }
            }.sortedWith(
                compareBy<SpeedTestResult> {
                    when (it.status) {
                        SpeedTestStatus.SUCCESS -> 0
                        SpeedTestStatus.TESTING -> 1
                        SpeedTestStatus.IDLE -> 2
                        SpeedTestStatus.TIMEOUT -> 3
                        SpeedTestStatus.ERROR -> 4
                    }
                }.thenBy { it.pingMs ?: Long.MAX_VALUE }
            )

            emit(updatedList)
        }
    }.flowOn(Dispatchers.IO)
}
