package com.afrouzi.dnsmaster.service

import android.net.VpnService
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class DnsPacketForwarder(
    private val vpnService: VpnService,
    private val vpnInput: FileInputStream,
    private val vpnOutput: FileOutputStream,
    private val primaryDnsIp: String,
    private val secondaryDnsIp: String = ""
) : Runnable {

    companion object {
        private const val TAG = "DnsPacketForwarder"
        private const val BUFFER_SIZE = 4096
        private const val UDP_PROTOCOL = 17
    }

    private val isRunning = AtomicBoolean(true)
    private var upstreamSocket: DatagramSocket? = null
    private val pendingQueries = ConcurrentHashMap<Int, ClientSession>()

    private data class ClientSession(
        val clientIp: ByteArray,
        val clientPort: Int,
        val serverIp: ByteArray,
        val serverPort: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun stop() {
        isRunning.set(false)
        try {
            upstreamSocket?.close()
        } catch (ignored: Exception) {}
    }

    override fun run() {
        Log.i(TAG, "DnsPacketForwarder started for DNS: $primaryDnsIp / $secondaryDnsIp")

        try {
            val socket = DatagramSocket()
            vpnService.protect(socket)
            socket.soTimeout = 2000
            upstreamSocket = socket

            // Thread for receiving upstream responses
            val receiverThread = Thread {
                val recvBuffer = ByteArray(BUFFER_SIZE)
                while (isRunning.get()) {
                    try {
                        val packet = DatagramPacket(recvBuffer, recvBuffer.size)
                        socket.receive(packet)
                        if (packet.length < 12) continue

                        // DNS Transaction ID is first 2 bytes of DNS payload
                        val id = ((recvBuffer[0].toInt() and 0xFF) shl 8) or (recvBuffer[1].toInt() and 0xFF)
                        val session = pendingQueries.remove(id) ?: continue

                        val dnsPayload = recvBuffer.copyOf(packet.length)
                        sendTunResponse(session, dnsPayload)
                    } catch (e: Exception) {
                        if (!isRunning.get()) break
                    }
                }
            }
            receiverThread.start()

            // Read from TUN interface
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
            Log.e(TAG, "Forwarder loop terminated", e)
        }
    }

    private fun handleTunPacket(buffer: ByteArray, length: Int, socket: DatagramSocket) {
        if (length < 28) return // Minimum IPv4 (20) + UDP (8)

        val version = (buffer[0].toInt() shr 4) and 0x0F
        if (version != 4) return // Only IPv4 currently

        val protocol = buffer[9].toInt() and 0xFF
        if (protocol != UDP_PROTOCOL) return

        val ipHeaderLength = (buffer[0].toInt() and 0x0F) * 4
        if (length < ipHeaderLength + 8) return

        val destPort = ((buffer[ipHeaderLength + 2].toInt() and 0xFF) shl 8) or
                (buffer[ipHeaderLength + 3].toInt() and 0xFF)

        if (destPort != 53) return // Only intercept DNS queries

        val srcPort = ((buffer[ipHeaderLength].toInt() and 0xFF) shl 8) or
                (buffer[ipHeaderLength + 1].toInt() and 0xFF)

        val srcIp = ByteArray(4) { i -> buffer[12 + i] }
        val dstIp = ByteArray(4) { i -> buffer[16 + i] }

        val dnsPayloadOffset = ipHeaderLength + 8
        val dnsPayloadLength = length - dnsPayloadOffset
        if (dnsPayloadLength < 12) return

        val dnsTransactionId = ((buffer[dnsPayloadOffset].toInt() and 0xFF) shl 8) or
                (buffer[dnsPayloadOffset + 1].toInt() and 0xFF)

        pendingQueries[dnsTransactionId] = ClientSession(
            clientIp = srcIp,
            clientPort = srcPort,
            serverIp = dstIp,
            serverPort = destPort
        )

        // Clean up queries older than 10 seconds
        val now = System.currentTimeMillis()
        pendingQueries.entries.removeIf { now - it.value.timestamp > 10000 }

        val dnsPayload = buffer.copyOfRange(dnsPayloadOffset, length)
        try {
            val targetIp = InetAddress.getByName(primaryDnsIp)
            val outPacket = DatagramPacket(dnsPayload, dnsPayload.size, targetIp, 53)
            socket.send(outPacket)
        } catch (e: Exception) {
            if (secondaryDnsIp.isNotBlank()) {
                try {
                    val fallbackIp = InetAddress.getByName(secondaryDnsIp)
                    val outPacket = DatagramPacket(dnsPayload, dnsPayload.size, fallbackIp, 53)
                    socket.send(outPacket)
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun sendTunResponse(session: ClientSession, dnsPayload: ByteArray) {
        val ipHeaderLength = 20
        val udpHeaderLength = 8
        val totalLength = ipHeaderLength + udpHeaderLength + dnsPayload.size
        val packet = ByteBuffer.allocate(totalLength)

        // 1. IPv4 Header
        packet.put(0x45.toByte()) // Version 4, IHL 5
        packet.put(0x00.toByte()) // Type of service
        packet.putShort(totalLength.toShort()) // Total length
        packet.putShort(0.toShort()) // Identification
        packet.putShort(0x4000.toShort()) // Flags: Don't Fragment
        packet.put(64.toByte()) // TTL
        packet.put(UDP_PROTOCOL.toByte()) // Protocol UDP (17)
        packet.putShort(0.toShort()) // Checksum placeholder
        packet.put(session.serverIp) // Source IP (virtual DNS IP)
        packet.put(session.clientIp) // Destination IP (client 10.0.0.2)

        // Compute IP Header Checksum
        val ipChecksum = computeIpChecksum(packet.array(), 0, ipHeaderLength)
        packet.putShort(10, ipChecksum.toShort())

        // 2. UDP Header
        packet.putShort(session.serverPort.toShort()) // Source Port (53)
        packet.putShort(session.clientPort.toShort()) // Dest Port
        packet.putShort((udpHeaderLength + dnsPayload.size).toShort()) // Length
        packet.putShort(0.toShort()) // UDP Checksum (0 is allowed in IPv4 UDP)

        // 3. DNS Payload
        packet.put(dnsPayload)

        synchronized(vpnOutput) {
            try {
                vpnOutput.write(packet.array(), 0, totalLength)
                vpnOutput.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write packet to TUN", e)
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
