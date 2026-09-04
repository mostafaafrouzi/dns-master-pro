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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.afrouzi.dnsmaster.theme.*
import com.afrouzi.dnsmaster.ui.components.*
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
        IosGroupedCard(cornerRadius = 18.dp) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isPersian) "بنچمارک و تست پینگ سرورها" else "DNS Speed Benchmark",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPersian)
                        "تست همزمان پینگ تمام سرورها برای یافتن سریع‌ترین DNS بر روی اینترنت شما"
                    else
                        "Benchmark all DNS providers simultaneously to find the lowest latency",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPersian) "در حال تست سرورها..." else "Benchmarking...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian) "شروع تست سرعت همزمان" else "Start Benchmark Test",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                if (isTesting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AppleBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        AnimatedVisibility(visible = fastestResult != null) {
            fastestResult?.let { fastest ->
                IosGroupedCard(
                    cornerRadius = 16.dp,
                    modifier = Modifier.border(1.dp, AppleOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
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
                                        .background(AppleOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = AppleOrange,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isPersian) "سریع‌ترین سرور پیشنهادی" else "Fastest Server Found",
                                    fontSize = 13.sp,
                                    color = AppleOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppleGreen.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = NetworkUtils.formatPing(fastest.pingMs),
                                    color = AppleGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fastest.dnsItem.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = fastest.dnsItem.primaryIp,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = { connectToDns(fastest.dnsItem) },
                                colors = ButtonDefaults.buttonColors(containerColor = AppleOrange, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersian) "اتصال فوری" else "Connect",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
        val categories = listOf(
            null to if (isPersian) "همه" else "All",
            com.afrouzi.dnsmaster.model.DnsCategory.ANTI_SANCTION.name to if (isPersian) "تحریم‌شکن" else "Anti-Sanction",
            com.afrouzi.dnsmaster.model.DnsCategory.GAMING.name to if (isPersian) "گیمینگ" else "Gaming",
            com.afrouzi.dnsmaster.model.DnsCategory.FAST.name to if (isPersian) "سریع" else "Fast",
            com.afrouzi.dnsmaster.model.DnsCategory.PRIVACY.name to if (isPersian) "امنیت" else "Privacy"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { (catKey, label) ->
                val isSelected = selectedCategoryFilter == catKey
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AppleBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { selectedCategoryFilter = catKey }
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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

            // Floating Refresh Button (iOS styled Apple Blue)
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
                containerColor = AppleBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 8.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Refresh Benchmark"
                    )
                }
            }
        }
    }
}
