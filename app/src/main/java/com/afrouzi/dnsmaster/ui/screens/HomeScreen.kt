package com.afrouzi.dnsmaster.ui.screens

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import com.afrouzi.dnsmaster.core.network.DnsLookupEngine
import com.afrouzi.dnsmaster.core.network.DnsLookupResult
import com.afrouzi.dnsmaster.util.NetworkDiagnosticsHelper
import com.afrouzi.dnsmaster.util.NetworkInfoState
import com.afrouzi.dnsmaster.util.NetworkTransport
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.core.utils.NetworkUtils
import com.afrouzi.dnsmaster.data.DefaultDnsServers
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.data.speedtest.DnsSpeedTester
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.VpnConnectionState
import com.afrouzi.dnsmaster.service.DnsVpnService
import com.afrouzi.dnsmaster.theme.*
import com.afrouzi.dnsmaster.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: DnsRepository,
    onNavigateToServers: () -> Unit,
    onNavigateToSpeedTest: () -> Unit,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val connectionState by DnsRepository.connectionState.collectAsState()
    val connectedDns by DnsRepository.connectedDns.collectAsState()
    val connectedStartTime by DnsRepository.connectedStartTime.collectAsState()

    val selectedDnsId by repository.selectedDnsIdFlow.collectAsState(initial = "cloudflare")
    val customList by repository.customDnsListFlow.collectAsState(initial = emptyList())

    val allServers = remember(customList) { customList + DefaultDnsServers.list }
    val currentDns = remember(selectedDnsId, allServers) {
        allServers.find { it.id == selectedDnsId } ?: DefaultDnsServers.list.first()
    }

    // Uptime ticker
    var uptimeString by remember { mutableStateOf("00:00:00") }
    var lastActiveDuration by remember { mutableStateOf("00:00:00") }
    LaunchedEffect(connectionState, connectedStartTime) {
        if (connectionState == VpnConnectionState.CONNECTED && connectedStartTime > 0) {
            while (true) {
                val elapsedSeconds = (System.currentTimeMillis() - connectedStartTime) / 1000
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                uptimeString = formatted
                lastActiveDuration = formatted
                delay(1000)
            }
        } else {
            uptimeString = "00:00:00"
        }
    }

    // Network Diagnostics state
    var networkInfo by remember { mutableStateOf(NetworkDiagnosticsHelper.getNetworkInfo(context)) }

    // Disconnection Session Summary state
    var wasConnected by remember { mutableStateOf(false) }
    var lastSessionDuration by remember { mutableStateOf("") }
    var lastSessionServerName by remember { mutableStateOf("") }
    var showDisconnectionDialog by remember { mutableStateOf(false) }

    // Live NSLookup Inspector state
    var lookupDomain by remember { mutableStateOf("google.com") }
    var isLookingUp by remember { mutableStateOf(false) }
    var lookupResult by remember { mutableStateOf<DnsLookupResult?>(null) }

    LaunchedEffect(connectionState) {
        networkInfo = NetworkDiagnosticsHelper.getNetworkInfo(context)
        if (connectionState == VpnConnectionState.CONNECTED) {
            wasConnected = true
        } else if (wasConnected && connectionState == VpnConnectionState.DISCONNECTED) {
            wasConnected = false
            if (lastActiveDuration != "00:00:00") {
                lastSessionDuration = lastActiveDuration
                lastSessionServerName = currentDns.name
                showDisconnectionDialog = true
                lastActiveDuration = "00:00:00"
            }
        }
    }

    // Live ping state
    var livePing by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(currentDns.id, connectionState) {
        if (connectionState == VpnConnectionState.CONNECTED) {
            livePing = DnsSpeedTester.pingDnsServer(currentDns.primaryIp)
        } else {
            livePing = null
        }
    }

    // VPN permission launcher
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent == null) {
            val startIntent = Intent(context, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_START
                putExtra(DnsVpnService.EXTRA_DNS_ID, currentDns.id)
            }
            context.startService(startIntent)
        }
    }

    fun toggleVpn() {
        if (connectionState == VpnConnectionState.CONNECTED) {
            val stopIntent = Intent(context, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_STOP
            }
            context.startService(stopIntent)
        } else {
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                vpnPrepareLauncher.launch(prepareIntent)
            } else {
                val startIntent = Intent(context, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                    putExtra(DnsVpnService.EXTRA_DNS_ID, currentDns.id)
                }
                context.startService(startIntent)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Connection Orb Button
        ConnectionOrb(
            connectionState = connectionState,
            onClick = { toggleVpn() },
            isPersian = isPersian
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Active DNS Card (iOS Inset Grouped Style)
        IosGroupedCard(
            cornerRadius = 18.dp,
            modifier = Modifier.clickable { onNavigateToServers() }
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(AppleBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (isPersian) "سرور انتخابی" else "Selected Server",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = currentDns.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isPersian) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // IPs and Metrics Grid (iOS Clean Metrics)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "آی‌پی اصلی" else "Primary IP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = currentDns.primaryIp,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isPersian) "مدت اتصال" else "Uptime",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = uptimeString,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (connectionState == VpnConnectionState.CONNECTED) AppleGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPersian) "پینگ زنده" else "Live Ping",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = NetworkUtils.formatPing(livePing),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (livePing != null) AppleGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Switch Header & Horizontal List
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isPersian) "سوییچ سریع دی‌ان‌اس" else "Quick Switch",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(
                onClick = onNavigateToSpeedTest,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = AppleBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPersian) "تست سرعت" else "Speed Test",
                    color = AppleBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val quickList = remember(allServers) {
            allServers.filter { it.isFavorite }.take(6)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickList, key = { it.id }) { item ->
                val isCurrent = item.id == currentDns.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrent) AppleBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = borderOrNull(isCurrent, AppleBlue),
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            repository.setSelectedDnsId(item.id)
                            if (connectionState == VpnConnectionState.CONNECTED) {
                                // Update running VPN with new DNS
                                val updateIntent = Intent(context, DnsVpnService::class.java).apply {
                                    action = DnsVpnService.ACTION_UPDATE
                                    putExtra(DnsVpnService.EXTRA_DNS_ID, item.id)
                                }
                                context.startService(updateIntent)
                            }
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isCurrent) AppleBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Network Diagnostics Card (iOS Inset Grouped Style)
        IosGroupedCard(cornerRadius = 18.dp) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val transportIcon = when (networkInfo.transportType) {
                            NetworkTransport.WIFI -> Icons.Default.Wifi
                            NetworkTransport.CELLULAR -> Icons.Default.SignalCellularAlt
                            else -> Icons.Default.Lan
                        }
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(AppleGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = transportIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isPersian) "تشخیص وضعیت شبکه" else "Network Diagnostics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (networkInfo.isInternetValidated) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppleGreen.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AppleGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersian) "آنلاین" else "Online",
                                    color = AppleGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "نوع اتصال" else "Connection Type",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian) networkInfo.transportNameFa else networkInfo.transportNameEn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPersian) "پروتکل‌های فعال" else "Supported Protocols",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (networkInfo.hasIpv4) AppleBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "IPv4",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (networkInfo.hasIpv4) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (networkInfo.hasIpv6) AppleBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "IPv6",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (networkInfo.hasIpv6) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (networkInfo.localIpAddress != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${if (isPersian) "آی‌پی محلی: " else "Local IP: "}${networkInfo.localIpAddress}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NSLookup Resolution Inspector Card (Real DNS Query Test)
        IosGroupedCard(cornerRadius = 18.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPersian) "تست زنده رزولوشن دامنه (NSLookup)" else "Live DNS Resolver (NSLookup)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPersian) "تست واقعی حل نام دامنه از روی سرور فعال" else "Real RFC 1035 query against active DNS",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isLookingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppleBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Domain Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickDomains = listOf("google.com", "docker.com", "shecan.ir", "wikipedia.org")
                    quickDomains.forEach { domain ->
                        val isSelected = lookupDomain == domain
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AppleBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { lookupDomain = domain }
                        ) {
                            Text(
                                text = domain,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Domain Input & Resolve Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = lookupDomain,
                        onValueChange = { lookupDomain = it },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (!isLookingUp && lookupDomain.isNotBlank()) {
                                isLookingUp = true
                                coroutineScope.launch {
                                    lookupResult = DnsLookupEngine.resolve(
                                        domain = lookupDomain,
                                        serverIp = currentDns.primaryIp
                                    )
                                    isLookingUp = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = if (isPersian) "تست" else "Resolve",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Results Container
                lookupResult?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    IosHairlineDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColor = if (res.isSuccess) AppleGreen else AppleRed
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = res.rCode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${res.latencyMs}ms",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            res.ttlSeconds?.let { ttl ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TTL: ${ttl}s",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (res.isAntiSanctionVerified) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AppleOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isPersian) "✓ عبور از تحریم" else "✓ Anti-Sanction",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (res.resolvedIps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPersian) "آی‌پی‌های دریافتی:" else "Resolved IPs:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            res.resolvedIps.take(3).forEach { ip ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = ip,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local Tunnel Guarantee Card (iOS Inset Grouped Style)
        IosGroupedCard(cornerRadius = 18.dp) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isPersian) "تضمین ۱۰۰٪ امنیت و تونل محلی" else "100% Local Tunnel Guarantee",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPersian)
                            "این اپلیکیشن ترافیک شخصی، دانلودها و وبگردی شما را از سرور واسط عبور نمی‌دهد. تنها کوئری‌های DNS به صورت کاملاً ایمن و محلی تغییر می‌یابند."
                        else
                            "DNS Master operates as a local resolver. Your downloads, browsing, and personal data are NEVER proxied through remote servers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Session Summary Dialog (iOS Modal Action Sheet Style)
    if (showDisconnectionDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AppleGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppleGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isPersian) "پایان نشست اتصال" else "Session Disconnected",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isPersian) "ارتباط با سرور دی‌ان‌اس با موفقیت قطع شد." else "Successfully disconnected from DNS server.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isPersian) "سرور:" else "Server:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Text(
                            text = lastSessionServerName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isPersian) "مدت اتصال:" else "Duration:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Text(
                            text = lastSessionDuration,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleGreen,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDisconnectionDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isPersian) "متوجه شدم" else "Got It", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

private fun borderOrNull(isSelected: Boolean, color: Color) =
    if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
