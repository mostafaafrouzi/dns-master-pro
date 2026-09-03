package com.afrouzi.dnsmaster

import com.afrouzi.dnsmaster.core.utils.NetworkUtils
import com.afrouzi.dnsmaster.data.DefaultDnsServers
import com.afrouzi.dnsmaster.model.DnsCategory
import org.junit.Assert.*
import org.junit.Test

class NetworkUtilsTest {

    @Test
    fun testValidIpv4Addresses() {
        assertTrue(NetworkUtils.isValidIpv4("1.1.1.1"))
        assertTrue(NetworkUtils.isValidIpv4("8.8.8.8"))
        assertTrue(NetworkUtils.isValidIpv4("178.22.122.100"))
        assertTrue(NetworkUtils.isValidIpv4("10.202.10.202"))
        assertTrue(NetworkUtils.isValidIpv4("0.0.0.0"))
        assertTrue(NetworkUtils.isValidIpv4("255.255.255.255"))
    }

    @Test
    fun testInvalidIpv4Addresses() {
        assertFalse(NetworkUtils.isValidIpv4(""))
        assertFalse(NetworkUtils.isValidIpv4("1.1.1"))
        assertFalse(NetworkUtils.isValidIpv4("1.1.1.1.1"))
        assertFalse(NetworkUtils.isValidIpv4("256.1.1.1"))
        assertFalse(NetworkUtils.isValidIpv4("abc.def.ghi.jkl"))
        assertFalse(NetworkUtils.isValidIpv4("1.1.1.-1"))
        assertFalse(NetworkUtils.isValidIpv4("01.1.1.1"))
    }

    @Test
    fun testValidIpv6Addresses() {
        assertTrue(NetworkUtils.isValidIpv6("2001:4860:4860::8888"))
        assertTrue(NetworkUtils.isValidIpv6("2606:4700:4700::1111"))
        assertTrue(NetworkUtils.isValidIpv6("::1"))
    }

    @Test
    fun testFormatPing() {
        assertEquals("25ms", NetworkUtils.formatPing(25L))
        assertEquals("0ms", NetworkUtils.formatPing(0L))
        assertEquals("--", NetworkUtils.formatPing(null))
        assertEquals("--", NetworkUtils.formatPing(-1L))
    }

    @Test
    fun testDefaultDnsServersIntegrity() {
        val servers = DefaultDnsServers.list
        assertTrue("Database should contain at least 20 servers", servers.size >= 20)

        for (server in servers) {
            assertTrue("ID should not be blank: ${server.name}", server.id.isNotBlank())
            assertTrue("Name should not be blank: ${server.id}", server.name.isNotBlank())
            assertTrue("Primary IP should be valid for ${server.name}: ${server.primaryIp}", NetworkUtils.isValidIp(server.primaryIp))
            if (server.secondaryIp.isNotBlank()) {
                assertTrue("Secondary IP should be valid for ${server.name}: ${server.secondaryIp}", NetworkUtils.isValidIp(server.secondaryIp))
            }
            assertTrue("Category should be recognized: ${server.category}", DnsCategory.values().any { it.name == server.category })
            assertTrue("Description Persian should not be blank for ${server.name}", server.descriptionFa.isNotBlank())
            assertTrue("Description English should not be blank for ${server.name}", server.descriptionEn.isNotBlank())
        }
    }
}
