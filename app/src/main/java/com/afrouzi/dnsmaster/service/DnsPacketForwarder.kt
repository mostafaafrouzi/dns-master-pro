package com.afrouzi.dnsmaster.service

import android.net.VpnService
import android.util.Log
import com.afrouzi.dnsmaster.core.network.DohResolver
import com.afrouzi.dnsmaster.core.network.LocalDnsCache
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class DnsPacketForwarder(
    private val vpnService: VpnService,
    private val vpnInput: FileInputStream,
    private val vpnOutput: FileOutputStream,
    private val primaryDnsIp: String,
    private val secondaryDnsIp: String = "",
    private val dohUrl: String? = null,
    private val isDohEnabled: Boolean = false,
    private val isLocalCacheEnabled: Boolean = true,
    private val onCacheHit: (() -> Unit)? = null
) : Runnable {

    companion object {
        private const val TAG = "DnsPacketForwarder"
        private const val BUFFER_SIZE = 4096
        private const val UDP_PROTOCOL = 17
    }

    private val isRunning = AtomicBoolean(true)
    private var upstreamSocket: DatagramSocket? = null
    private val pendingQueries = ConcurrentHashMap<Int, ClientSession>()
    private val dnsCache = LocalDnsCache(500)
    private val dohExecutor = Executors.newFixedThreadPool(4)

    private val primaryAddress: InetAddress? by lazy {
        try {
            InetAddress.getByName(primaryDnsIp.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Invalid primary DNS: $primaryDnsIp", e)
            null
        }
    }

    private val secondaryAddress: InetAddress? by lazy {
        if (secondaryDnsIp.isNotBlank()) {
            try {
                InetAddress.getByName(secondaryDnsIp.trim())
            } catch (e: Exception) {
                Log.e(TAG, "Invalid secondary DNS: $secondaryDnsIp", e)
                null
            }
        } else null
    }

    private data class ClientSession(
        val clientIp: ByteArray,
        val clientPort: Int,
        val serverIp: ByteArray,
        val serverPort: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun stop() {
        Log.i(TAG, "Stopping DnsPacketForwarder")
        isRunning.set(false)
        try {
            upstreamSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing upstream socket", e)
        }
        try {
            dohExecutor.shutdownNow()
        } catch (ignored: Exception) {}
    }

    override fun run() {
        Log.i(TAG, "DnsPacketForwarder started. Primary: $primaryDnsIp, Secondary: $secondaryDnsIp, DoH: $isDohEnabled ($dohUrl), Cache: $isLocalCacheEnabled")

        try {
            val socket = DatagramSocket()
            if (!vpnService.protect(socket)) {
                Log.e(TAG, "Failed to protect DatagramSocket")
            }
            socket.soTimeout = 3000
            upstreamSocket = socket

            val receiverThread = Thread({
                val recvBuffer = ByteArray(BUFFER_SIZE)
                while (isRunning.get()) {
                    try {
                        val packet = DatagramPacket(recvBuffer, recvBuffer.size)
                        socket.receive(packet)
                        if (packet.length < 12) continue

                        val id = ((recvBuffer[0].toInt() and 0xFF) shl 8) or (recvBuffer[1].toInt() and 0xFF)
                        val session = pendingQueries.remove(id) ?: continue

                        val dnsPayload = recvBuffer.copyOf(packet.length)
                        if (isLocalCacheEnabled) {
                            val qKey = dnsCache.extractQuestionKey(dnsPayload)
                            if (qKey != null) {
                                dnsCache.put(qKey, dnsPayload)
                            }
                        }
                        sendTunResponse(session, dnsPayload)
                    } catch (e: SocketTimeoutException) {
                        continue
                    } catch (e: Exception) {
                        if (!isRunning.get() || socket.isClosed) break
                        Log.e(TAG, "Receiver thread error", e)
                    }
                }
            }, "DnsReceiverThread")
            receiverThread.start()

            val packetBuffer = ByteArray(BUFFER_SIZE)
            while (isRunning.get()) {
                val length = try {
                    vpnInput.read(packetBuffer)
                } catch (e: Exception) {
                    break
                }
                if (length <= 0) continue

                handleTunPacket(packetBuffer, length, socket)
            }
            receiverThread.interrupt()
        } catch (e: Exception) {
            Log.e(TAG, "Forwarder loop fatal error", e)
        } finally {
            Log.i(TAG, "DnsPacketForwarder shutting down")
            try {
                upstreamSocket?.close()
            } catch (e: Exception) {}
            try {
                dohExecutor.shutdownNow()
            } catch (ignored: Exception) {}
        }
    }

    private fun handleTunPacket(buffer: ByteArray, length: Int, socket: DatagramSocket) {
        if (length < 28) return

        val version = (buffer[0].toInt() shr 4) and 0x0F
        if (version != 4) return

        val protocol = buffer[9].toInt() and 0xFF
        if (protocol != UDP_PROTOCOL) return

        val ipHeaderLength = (buffer[0].toInt() and 0x0F) * 4
        val destPort = ((buffer[ipHeaderLength + 2].toInt() and 0xFF) shl 8) or (buffer[ipHeaderLength + 3].toInt() and 0xFF)
        if (destPort != 53) return

        val srcPort = ((buffer[ipHeaderLength].toInt() and 0xFF) shl 8) or (buffer[ipHeaderLength + 1].toInt() and 0xFF)
        val srcIp = buffer.copyOfRange(12, 16)
        val dstIp = buffer.copyOfRange(16, 20)
        val dnsPayloadOffset = ipHeaderLength + 8
        val dnsPayload = buffer.copyOfRange(dnsPayloadOffset, length)
        if (dnsPayload.size < 12) return

        val dnsTransactionId = ((dnsPayload[0].toInt() and 0xFF) shl 8) or (dnsPayload[1].toInt() and 0xFF)

        // 1. Check Local Cache with instant 0ms response
        val questionKey = if (isLocalCacheEnabled) dnsCache.extractQuestionKey(dnsPayload) else null
        if (questionKey != null) {
            val cachedResponse = dnsCache.get(questionKey, dnsTransactionId)
            if (cachedResponse != null) {
                val session = ClientSession(srcIp, srcPort, dstIp, destPort)
                sendTunResponse(session, cachedResponse)
                onCacheHit?.invoke()
                return
            }
        }

        val session = ClientSession(srcIp, srcPort, dstIp, destPort)

        // 2. DoH Mode (RFC 8484)
        if (isDohEnabled && !dohUrl.isNullOrBlank()) {
            dohExecutor.execute {
                val dohResponse = DohResolver.resolve(dohUrl, dnsPayload)
                if (dohResponse != null && dohResponse.size >= 12) {
                    dohResponse[0] = ((dnsTransactionId shr 8) and 0xFF).toByte()
                    dohResponse[1] = (dnsTransactionId and 0xFF).toByte()
                    if (isLocalCacheEnabled && questionKey != null) {
                        dnsCache.put(questionKey, dohResponse)
                    }
                    sendTunResponse(session, dohResponse)
                    return@execute
                }
                // Fallback to UDP if DoH fails
                forwardUdp(dnsPayload, dnsTransactionId, session, socket)
            }
            return
        }

        // 3. Direct UDP query
        forwardUdp(dnsPayload, dnsTransactionId, session, socket)
    }

    private fun forwardUdp(
        dnsPayload: ByteArray,
        dnsTransactionId: Int,
        session: ClientSession,
        socket: DatagramSocket
    ) {
        pendingQueries[dnsTransactionId] = session

        // Clean queries older than 8 seconds
        val now = System.currentTimeMillis()
        pendingQueries.entries.removeIf { now - it.value.timestamp > 8000 }

        val target = primaryAddress
        if (target != null) {
            try {
                socket.send(DatagramPacket(dnsPayload, dnsPayload.size, target, 53))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to forward DNS query to primary, trying secondary", e)
                val fallback = secondaryAddress
                if (fallback != null) {
                    try {
                        socket.send(DatagramPacket(dnsPayload, dnsPayload.size, fallback, 53))
                    } catch (e2: Exception) {
                        Log.e(TAG, "Failed to forward DNS query to secondary", e2)
                    }
                }
            }
        }
    }

    private fun sendTunResponse(session: ClientSession, dnsPayload: ByteArray) {
        val ipHeaderLength = 20
        val udpHeaderLength = 8
        val totalLength = ipHeaderLength + udpHeaderLength + dnsPayload.size
        val packet = ByteBuffer.allocate(totalLength)

        // 1. IPv4 Header
        packet.put(0x45.toByte())
        packet.put(0x00.toByte())
        packet.putShort(totalLength.toShort())
        packet.putShort(0.toShort())
        packet.putShort(0x4000.toShort()) // DF flag
        packet.put(64.toByte()) // TTL
        packet.put(UDP_PROTOCOL.toByte())
        packet.putShort(0.toShort()) // checksum placeholder
        packet.put(session.serverIp) // source IP (virtual DNS IP)
        packet.put(session.clientIp) // destination IP (client)

        val ipChecksum = computeIpChecksum(packet.array(), 0, ipHeaderLength)
        packet.putShort(10, ipChecksum.toShort())

        // 2. UDP Header
        val udpStart = ipHeaderLength
        packet.putShort(udpStart, session.serverPort.toShort())
        packet.putShort(udpStart + 2, session.clientPort.toShort())
        packet.putShort(udpStart + 4, (udpHeaderLength + dnsPayload.size).toShort())
        packet.putShort(udpStart + 6, 0.toShort()) // UDP checksum 0 is valid in IPv4

        // 3. DNS Payload
        packet.position(udpStart + udpHeaderLength)
        packet.put(dnsPayload)

        synchronized(vpnOutput) {
            try {
                vpnOutput.write(packet.array(), 0, totalLength)
                vpnOutput.flush()
            } catch (e: Exception) {
                if (isRunning.get()) {
                    Log.e(TAG, "Error writing DNS response to TUN", e)
                }
            }
        }
    }

    private fun computeIpChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var i = offset
        while (i < offset + length) {
            val high = data[i].toInt() and 0xFF
            val low = if (i + 1 < offset + length) data[i + 1].toInt() and 0xFF else 0
            sum += (high shl 8) or low
            i += 2
        }
        while ((sum shr 16) > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return (sum.inv()) and 0xFFFF
    }
}
