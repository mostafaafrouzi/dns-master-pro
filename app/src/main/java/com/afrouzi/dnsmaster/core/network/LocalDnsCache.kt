package com.afrouzi.dnsmaster.core.network

import android.util.Log
import java.util.Collections
import java.util.LinkedHashMap
import java.util.concurrent.atomic.AtomicLong

class LocalDnsCache(private val maxEntries: Int = 1000) {

    companion object {
        private const val TAG = "LocalDnsCache"
        private const val DEFAULT_TTL_SECONDS = 300L
        private const val MIN_TTL_SECONDS = 10L
        private const val MAX_TTL_SECONDS = 86400L
    }

    data class CachedResponse(
        val responseBytes: ByteArray,
        val expireAtMillis: Long,
        val ttlSeconds: Long
    )

    private val lock = Any()
    private val lruMap = Collections.synchronizedMap(
        object : LinkedHashMap<String, CachedResponse>(maxEntries, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedResponse>?): Boolean {
                return size > maxEntries
            }
        }
    )

    val hitsCount = AtomicLong(0)
    val missesCount = AtomicLong(0)

    fun clear() {
        synchronized(lock) {
            lruMap.clear()
            hitsCount.set(0)
            missesCount.set(0)
        }
    }

    fun size(): Int = lruMap.size

    /**
     * Extracts Question Key (qname + "_" + qtype) from query payload.
     */
    fun extractQuestionKey(dnsPayload: ByteArray): String? {
        if (dnsPayload.size < 12) return null
        try {
            var offset = 12
            val nameBuilder = StringBuilder()

            while (offset < dnsPayload.size) {
                val len = dnsPayload[offset].toInt() and 0xFF
                if (len == 0) {
                    offset++
                    break
                }
                // DNS pointer in question section is invalid
                if ((len and 0xC0) != 0) return null
                offset++
                if (offset + len > dnsPayload.size) return null

                if (nameBuilder.isNotEmpty()) nameBuilder.append(".")
                nameBuilder.append(String(dnsPayload, offset, len, Charsets.US_ASCII))
                offset += len
            }

            if (offset + 4 > dnsPayload.size) return null
            val qType = ((dnsPayload[offset].toInt() and 0xFF) shl 8) or (dnsPayload[offset + 1].toInt() and 0xFF)
            val domain = nameBuilder.toString().lowercase()
            return "${domain}_$qType"
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Retrieves a cached response if valid, and updates transaction ID to match client's query.
     */
    fun get(key: String, clientTransactionId: Int): ByteArray? {
        val entry = lruMap[key] ?: run {
            missesCount.incrementAndGet()
            return null
        }

        val now = System.currentTimeMillis()
        if (now >= entry.expireAtMillis) {
            lruMap.remove(key)
            missesCount.incrementAndGet()
            return null
        }

        hitsCount.incrementAndGet()
        val cloned = entry.responseBytes.clone()
        if (cloned.size >= 2) {
            cloned[0] = ((clientTransactionId shr 8) and 0xFF).toByte()
            cloned[1] = (clientTransactionId and 0xFF).toByte()
        }
        return cloned
    }

    /**
     * Parses the lowest TTL from the response payload and caches the response.
     */
    fun put(key: String, responsePayload: ByteArray) {
        if (responsePayload.size < 12) return
        try {
            val anCount = ((responsePayload[6].toInt() and 0xFF) shl 8) or (responsePayload[7].toInt() and 0xFF)
            if (anCount <= 0) return

            val lowestTtl = extractLowestTtl(responsePayload) ?: DEFAULT_TTL_SECONDS
            val clampedTtl = lowestTtl.coerceIn(MIN_TTL_SECONDS, MAX_TTL_SECONDS)
            val expireAt = System.currentTimeMillis() + (clampedTtl * 1000L)

            lruMap[key] = CachedResponse(
                responseBytes = responsePayload.clone(),
                expireAtMillis = expireAt,
                ttlSeconds = clampedTtl
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache DNS response: ${e.message}")
        }
    }

    private fun extractLowestTtl(data: ByteArray): Long? {
        var offset = 12
        val qdCount = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
        val anCount = ((data[6].toInt() and 0xFF) shl 8) or (data[7].toInt() and 0xFF)

        // Skip Question Section
        for (i in 0 until qdCount) {
            while (offset < data.size) {
                val len = data[offset].toInt() and 0xFF
                if (len == 0) { offset++; break }
                if ((len and 0xC0) == 0xC0) { offset += 2; break }
                offset += 1 + len
            }
            offset += 4 // QTYPE (2) + QCLASS (2)
            if (offset >= data.size) return null
        }

        var minTtl: Long = Long.MAX_VALUE

        // Parse Answer Section
        for (i in 0 until anCount) {
            if (offset >= data.size) break
            // Skip Name
            while (offset < data.size) {
                val len = data[offset].toInt() and 0xFF
                if (len == 0) { offset++; break }
                if ((len and 0xC0) == 0xC0) { offset += 2; break }
                offset += 1 + len
            }
            if (offset + 10 > data.size) break
            // Type (2), Class (2), TTL (4), RdLength (2)
            val ttl = ((data[offset + 4].toLong() and 0xFF) shl 24) or
                    ((data[offset + 5].toLong() and 0xFF) shl 16) or
                    ((data[offset + 6].toLong() and 0xFF) shl 8) or
                    (data[offset + 7].toLong() and 0xFF)

            if (ttl in 1 until minTtl) {
                minTtl = ttl
            }

            val rdLength = ((data[offset + 8].toInt() and 0xFF) shl 8) or (data[offset + 9].toInt() and 0xFF)
            offset += 10 + rdLength
        }

        return if (minTtl != Long.MAX_VALUE) minTtl else null
    }
}
