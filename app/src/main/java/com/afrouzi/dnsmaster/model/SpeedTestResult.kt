package com.afrouzi.dnsmaster.model

enum class SpeedTestStatus {
    IDLE,
    TESTING,
    SUCCESS,
    TIMEOUT,
    ERROR
}

data class SpeedTestResult(
    val dnsItem: DnsItem,
    val pingMs: Long? = null,
    val status: SpeedTestStatus = SpeedTestStatus.IDLE,
    val isFastest: Boolean = false,
    val packetLoss: Int? = null,
    val jitterMs: Long? = null
)
