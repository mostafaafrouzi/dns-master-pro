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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
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
import com.afrouzi.dnsmaster.ui.components.ConnectionOrb
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

        // Active DNS Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .clickable { onNavigateToServers() }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isPersian) "سرور انتخابی" else "Selected Server",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentDns.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                // IPs and Metrics Grid
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "آی‌پی اصلی" else "Primary IP",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentDns.primaryIp,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = if (isPersian) "مدت اتصال" else "Uptime",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uptimeString,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (connectionState == VpnConnectionState.CONNECTED) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column {
                        Text(
                            text = if (isPersian) "پینگ زنده" else "Live Ping",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = NetworkUtils.formatPing(livePing),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (livePing != null) PingGreat else MaterialTheme.colorScheme.onSurfaceVariant
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(onClick = onNavigateToSpeedTest) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPersian) "تست سرعت" else "Speed Test",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val quickList = remember(allServers) {
            allServers.filter { it.isFavorite }.take(6)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickList, key = { it.id }) { item ->
                val isCurrent = item.id == currentDns.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = borderOrNull(isCurrent, NeonCyan),
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
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Network Diagnostics Card (Inspired by competitor)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
        ) {
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
                        Icon(
                            imageVector = transportIcon,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian) "تشخیص وضعیت شبکه" else "Network Diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (networkInfo.isInternetValidated) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonEmerald.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (isPersian) "آنلاین" else "Online",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "نوع اتصال" else "Connection Type",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (networkInfo.hasIpv4) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "IPv4",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (networkInfo.hasIpv4) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (networkInfo.hasIpv6) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "IPv6",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (networkInfo.hasIpv6) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (networkInfo.localIpAddress != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${if (isPersian) "آی‌پی محلی: " else "Local IP: "}${networkInfo.localIpAddress}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local Tunnel Guarantee Card (Inspired by competitor)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isPersian) "تضمین ۱۰۰٪ امنیت و تونل محلی" else "100% Local Tunnel Guarantee",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPersian)
                            "این اپلیکیشن ترافیک شخصی، دانلودها و وبگردی شما را از سرور واسط عبور نمی‌دهد. تنها کوئری‌های DNS به صورت کاملاً ایمن و محلی تغییر می‌یابند."
                        else
                            "DNS Master operates as a local resolver. Your downloads, browsing, and personal data are NEVER proxied through remote servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Session Summary Dialog (Inspired by competitor)
    if (showDisconnectionDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "پایان نشست اتصال" else "Session Disconnected",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isPersian) "ارتباط با سرور دی‌ان‌اس با موفقیت قطع شد." else "Successfully disconnected from DNS server.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                            fontWeight = FontWeight.Bold,
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
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDisconnectionDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isPersian) "متوجه شدم" else "Got It", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

private fun borderOrNull(isSelected: Boolean, color: Color) =
    if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
