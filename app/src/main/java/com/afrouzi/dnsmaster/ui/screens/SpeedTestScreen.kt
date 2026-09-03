package com.afrouzi.dnsmaster.ui.screens

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
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
import com.afrouzi.dnsmaster.model.SpeedTestResult
import com.afrouzi.dnsmaster.model.SpeedTestStatus
import com.afrouzi.dnsmaster.model.VpnConnectionState
import com.afrouzi.dnsmaster.service.DnsVpnService
import com.afrouzi.dnsmaster.theme.NeonAmber
import com.afrouzi.dnsmaster.theme.NeonCyan
import com.afrouzi.dnsmaster.theme.PingGreat
import com.afrouzi.dnsmaster.ui.components.SpeedTestItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SpeedTestScreen(
    repository: DnsRepository,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val customList by repository.customDnsListFlow.collectAsState(initial = emptyList())
    val connectionState by DnsRepository.connectionState.collectAsState()

    val allServers = remember(customList) { customList + DefaultDnsServers.list }

    var isTesting by remember { mutableStateOf(false) }
    var testResults by remember {
        mutableStateOf(allServers.map { SpeedTestResult(it, null, SpeedTestStatus.IDLE) })
    }

    var selectedItemForVpn by remember { mutableStateOf<com.afrouzi.dnsmaster.model.DnsItem?>(null) }

    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val target = selectedItemForVpn ?: return@rememberLauncherForActivityResult
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent == null) {
            val startIntent = Intent(context, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_START
                putExtra(DnsVpnService.EXTRA_DNS_ID, target.id)
            }
            context.startService(startIntent)
        }
    }

    val connectToDns: (com.afrouzi.dnsmaster.model.DnsItem) -> Unit = { item ->
        coroutineScope.launch {
            repository.setSelectedDnsId(item.id)
            if (connectionState == VpnConnectionState.CONNECTED) {
                val updateIntent = Intent(context, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_UPDATE
                    putExtra(DnsVpnService.EXTRA_DNS_ID, item.id)
                }
                context.startService(updateIntent)
            } else {
                selectedItemForVpn = item
                val prepareIntent = VpnService.prepare(context)
                if (prepareIntent != null) {
                    vpnPrepareLauncher.launch(prepareIntent)
                } else {
                    val startIntent = Intent(context, DnsVpnService::class.java).apply {
                        action = DnsVpnService.ACTION_START
                        putExtra(DnsVpnService.EXTRA_DNS_ID, item.id)
                    }
                    context.startService(startIntent)
                }
            }
        }
    }

    val fastestResult = remember(testResults) {
        testResults.find { it.isFastest && it.status == SpeedTestStatus.SUCCESS }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Benchmark Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isPersian) "بنچمارک و تست پینگ سرورها" else "DNS Speed Benchmark",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPersian)
                        "تست همزمان پینگ تمام سرورها برای یافتن سریع‌ترین DNS بر روی اینترنت شما"
                    else
                        "Benchmark all DNS providers simultaneously to find the lowest latency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isTesting) {
                            isTesting = true
                            coroutineScope.launch {
                                DnsSpeedTester.benchmarkAll(allServers).collectLatest { updatedList ->
                                    testResults = updatedList
                                }
                                isTesting = false
                            }
                        }
                    },
                    enabled = !isTesting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPersian) "در حال تست سرورها..." else "Benchmarking...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian) "شروع تست سرعت همزمان" else "Start Benchmark Test",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                if (isTesting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fastest Server Spotlight Banner
        AnimatedVisibility(visible = fastestResult != null) {
            fastestResult?.let { fastest ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B10)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonAmber, RoundedCornerShape(18.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPersian) "سریع‌ترین سرور پیشنهادی" else "Fastest Server Found",
                                    fontSize = 12.sp,
                                    color = NeonAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${fastest.dnsItem.name} (${NetworkUtils.formatPing(fastest.pingMs)})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { connectToDns(fastest.dnsItem) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPersian) "اتصال فوری" else "Connect",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
        val categories = listOf(
            null to if (isPersian) "همه" else "All",
            com.afrouzi.dnsmaster.model.DnsCategory.ANTI_SANCTION.name to if (isPersian) "تحریم‌شکن" else "Anti-Sanction",
            com.afrouzi.dnsmaster.model.DnsCategory.GAMING.name to if (isPersian) "گیمینگ" else "Gaming",
            com.afrouzi.dnsmaster.model.DnsCategory.FAST.name to if (isPersian) "سریع" else "Fast",
            com.afrouzi.dnsmaster.model.DnsCategory.PRIVACY.name to if (isPersian) "امنیت" else "Privacy"
        )

        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { (catKey, label) ->
                FilterChip(
                    selected = selectedCategoryFilter == catKey,
                    onClick = { selectedCategoryFilter = catKey },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filteredResults = remember(testResults, selectedCategoryFilter) {
            if (selectedCategoryFilter == null) {
                testResults
            } else {
                testResults.filter { it.dnsItem.category == selectedCategoryFilter }
            }
        }

        // Leaderboard List with FAB
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredResults, key = { it.dnsItem.id }) { item ->
                    SpeedTestItem(
                        result = item,
                        onApply = { connectToDns(item.dnsItem) },
                        isPersian = isPersian
                    )
                }
            }

            // Floating Refresh Button (Inspired by competitor)
            FloatingActionButton(
                onClick = {
                    if (!isTesting) {
                        isTesting = true
                        coroutineScope.launch {
                            DnsSpeedTester.benchmarkAll(allServers).collectLatest { updatedList ->
                                testResults = updatedList
                            }
                            isTesting = false
                        }
                    }
                },
                containerColor = NeonCyan,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 8.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Bolt,
                        contentDescription = "Refresh Benchmark"
                    )
                }
            }
        }
    }
}
