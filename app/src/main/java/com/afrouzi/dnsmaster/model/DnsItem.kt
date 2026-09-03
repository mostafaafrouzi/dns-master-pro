package com.afrouzi.dnsmaster.model

import kotlinx.serialization.Serializable

@Serializable
data class DnsItem(
    val id: String,
    val name: String,
    val primaryIp: String,
    val secondaryIp: String = "",
    val category: String = DnsCategory.FAST.name,
    val descriptionEn: String = "",
    val descriptionFa: String = "",
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val dohUrl: String? = null,
    val pingMs: Long? = null,
    val primaryIpv6: String? = null,
    val secondaryIpv6: String? = null
)
