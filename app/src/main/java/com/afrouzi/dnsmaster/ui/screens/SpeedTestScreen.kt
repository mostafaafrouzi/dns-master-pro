package com.afrouzi.dnsmaster.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.core.network.DnsLookupEngine
import com.afrouzi.dnsmaster.core.network.DnsLookupResult
import com.afrouzi.dnsmaster.core.utils.NetworkUtils
import com.afrouzi.dnsmaster.data.DefaultDnsServers
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.data.speedtest.DnsSpeedTester
import com.afrouzi.dnsmaster.model.DnsItem
import com.afrouzi.dnsmaster.model.SpeedTestResult
import com.afrouzi.dnsmaster.model.SpeedTestStatus
import com.afrouzi.dnsmaster.model.VpnConnectionState
import com.afrouzi.dnsmaster.service.DnsVpnService
import com.afrouzi.dnsmaster.theme.*
import com.afrouzi.dnsmaster.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdvancedLookupItemState(
    val dnsItem: DnsItem,
    val lookupResult: DnsLookupResult? = null,
    val status: SpeedTestStatus = SpeedTestStatus.IDLE,
    val isFastest: Boolean = false
)

@Composable
fun SpeedTestScreen(
    repository: DnsRepository,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val customList by repository.customDnsListFlow.collectAsState(initial = emptyList())
    val connectionState by DnsRepository.connectionState.collectAsState()

    val allServers = remember(customList) { customList + DefaultDnsServers.list }

    // Test Modes: "ping" vs "nslookup"
    var testMode by remember { mutableStateOf("nslookup") }

    // Ping State
    var isPingTesting by remember { mutableStateOf(false) }
    var pingResults by remember {
        mutableStateOf(allServers.map { SpeedTestResult(it, null, SpeedTestStatus.IDLE) })
    }

    // NSLookup State
    var targetDomain by remember { mutableStateOf("google.com") }
    var isLookupTesting by remember { mutableStateOf(false) }
    var lookupResults by remember {
        mutableStateOf(allServers.map { AdvancedLookupItemState(it, null, SpeedTestStatus.IDLE) })
    }

    var selectedItemForVpn by remember { mutableStateOf<DnsItem?>(null) }

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

    val connectToDns: (DnsItem) -> Unit = { item ->
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

    // Start NSLookup Benchmark
    val runNsLookupBenchmark: () -> Unit = {
        if (!isLookupTesting) {
            focusManager.clearFocus()
            isLookupTesting = true
            coroutineScope.launch {
                // Mark all as testing
                lookupResults = lookupResults.map { it.copy(status = SpeedTestStatus.TESTING) }

                val resolved = withContext(Dispatchers.IO) {
                    allServers.map { server ->
                        async {
                            val res = DnsLookupEngine.resolve(
                                domain = targetDomain.trim(),
                                serverIp = server.primaryIp,
                                timeoutMs = 3000
                            )
                            val status = if (res.isSuccess) SpeedTestStatus.SUCCESS else SpeedTestStatus.TIMEOUT
                            AdvancedLookupItemState(
                                dnsItem = server,
                                lookupResult = res,
                                status = status,
                                isFastest = false
                            )
                        }
                    }.awaitAll()
                }

                // Find fastest among successful
                val minLatency = resolved
                    .filter { it.status == SpeedTestStatus.SUCCESS && it.lookupResult != null }
                    .minOfOrNull { it.lookupResult!!.latencyMs }

                val sorted = resolved.sortedWith(
                    compareBy<AdvancedLookupItemState> {
                        if (it.status == SpeedTestStatus.SUCCESS) 0 else 1
                    }.thenBy {
                        it.lookupResult?.latencyMs ?: Long.MAX_VALUE
                    }
                ).map { item ->
                    val isFastest = item.status == SpeedTestStatus.SUCCESS &&
                            item.lookupResult != null &&
                            item.lookupResult.latencyMs == minLatency
                    item.copy(isFastest = isFastest)
                }

                lookupResults = sorted
                isLookupTesting = false
            }
        }
    }

    // Ping Fastest
    val fastestPing = remember(pingResults) {
        pingResults.find { it.isFastest && it.status == SpeedTestStatus.SUCCESS }
    }

    // NSLookup Fastest
    val fastestLookup = remember(lookupResults) {
        lookupResults.find { it.isFastest && it.status == SpeedTestStatus.SUCCESS }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mode Selector: NSLookup vs Ping
        IosSegmentedControl(
            items = listOf("nslookup", "ping"),
            selectedItem = testMode,
            onItemSelected = { testMode = it },
            itemLabel = {
                if (it == "nslookup") (if (isPersian) "تست رزولوشن (NSLookup)" else "NSLookup Resolver")
                else (if (isPersian) "بنچمارک پینگ (Ping)" else "Ping Benchmark")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (testMode == "nslookup") {
            // NSLookup Benchmark Card
            IosGroupedCard(cornerRadius = 18.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = AppleBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPersian) "تست واقعی رزولوشن (مانند nslookup ویندوز)" else "Real RFC 1035 NSLookup Benchmark",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPersian)
                                    "ارسال پکت کوئری UDP به سرورها و استخراج واقعی IP و وضعیت RCODE"
                                else
                                    "Sends raw DNS query packets and verifies IP resolution & TTL",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Domain Input Field
                    OutlinedTextField(
                        value = targetDomain,
                        onValueChange = { targetDomain = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("domain.com") },
                        label = { Text(if (isPersian) "دامنه برای تست پاسخگویی" else "Target Domain to Resolve") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { runNsLookupBenchmark() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Chips
                    val presetDomains = listOf(
                        "google.com" to "Google",
                        "docker.com" to (if (isPersian) "تحریم‌شکن (Docker)" else "Anti-Sanction"),
                        "shecan.ir" to "Shecan",
                        "wikipedia.org" to "Wikipedia",
                        "cloudflare.com" to "Cloudflare"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetDomains) { (domain, label) ->
                            val isSelected = targetDomain == domain
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AppleBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable {
                                    targetDomain = domain
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = runNsLookupBenchmark,
                        enabled = !isLookupTesting && targetDomain.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        if (isLookupTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isPersian) "در حال تست NSLookup سرورها..." else "Querying DNS Servers...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersian) "شروع تست رزولوشن دامنه" else "Run NSLookup Benchmark",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (isLookupTesting) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AppleBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fastest NSLookup Banner
            AnimatedVisibility(visible = fastestLookup != null) {
                fastestLookup?.let { fastest ->
                    IosGroupedCard(
                        cornerRadius = 14.dp,
                        modifier = Modifier.border(1.dp, AppleGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(AppleGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = AppleGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${if (isPersian) "سریع‌ترین پاسخ:" else "Fastest Resolver:"} ${fastest.dnsItem.name}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${fastest.lookupResult?.latencyMs ?: 0}ms • RCODE ${fastest.lookupResult?.rCode ?: "OK"}",
                                        fontSize = 11.sp,
                                        color = AppleGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = { connectToDns(fastest.dnsItem) },
                                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (isPersian) "اتصال" else "Connect",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // NSLookup Results List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(lookupResults, key = { it.dnsItem.id }) { item ->
                    AdvancedLookupCard(
                        state = item,
                        onConnect = { connectToDns(item.dnsItem) },
                        isPersian = isPersian
                    )
                }
            }

        } else {
            // Ping Benchmark Mode
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
                            if (!isPingTesting) {
                                isPingTesting = true
                                coroutineScope.launch {
                                    DnsSpeedTester.benchmarkAll(allServers).collectLatest { updatedList ->
                                        pingResults = updatedList
                                    }
                                    isPingTesting = false
                                }
                            }
                        },
                        enabled = !isPingTesting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isPingTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
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

                    if (isPingTesting) {
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

            AnimatedVisibility(visible = fastestPing != null) {
                fastestPing?.let { fastest ->
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

            val filteredResults = remember(pingResults, selectedCategoryFilter) {
                if (selectedCategoryFilter == null) {
                    pingResults
                } else {
                    pingResults.filter { it.dnsItem.category == selectedCategoryFilter }
                }
            }

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

                FloatingActionButton(
                    onClick = {
                        if (!isPingTesting) {
                            isPingTesting = true
                            coroutineScope.launch {
                                DnsSpeedTester.benchmarkAll(allServers).collectLatest { updatedList ->
                                    pingResults = updatedList
                                }
                                isPingTesting = false
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
                    if (isPingTesting) {
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
}

@Composable
private fun AdvancedLookupCard(
    state: AdvancedLookupItemState,
    onConnect: () -> Unit,
    isPersian: Boolean
) {
    val borderColor = if (state.isFastest) AppleOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val cardBackground = if (state.isFastest) AppleOrange.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (state.isFastest) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Server name & status
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (state.isFastest) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Fastest",
                            tint = AppleOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = state.dnsItem.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.dnsItem.primaryIp,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Latency / Status Badge
                when (state.status) {
                    SpeedTestStatus.TESTING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = AppleBlue
                        )
                    }
                    SpeedTestStatus.SUCCESS -> {
                        val latency = state.lookupResult?.latencyMs ?: 0L
                        val badgeColor = when {
                            latency < 80 -> AppleGreen
                            latency < 200 -> AppleBlue
                            else -> AppleOrange
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${latency}ms",
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onConnect,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isFastest) AppleOrange else AppleBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (isPersian) "اتصال" else "Use",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    SpeedTestStatus.TIMEOUT -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppleRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isPersian) "پاسخ نداد (Timeout)" else "Timeout",
                                color = AppleRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    SpeedTestStatus.ERROR,
                    SpeedTestStatus.IDLE -> {
                        Text(
                            text = "--",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // If resolved, show extra details (resolved IPs, TTL, Anti-Sanction)
            state.lookupResult?.let { res ->
                if (res.isSuccess) {
                    Spacer(modifier = Modifier.height(10.dp))
                    IosHairlineDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Resolved IPs
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${if (isPersian) "آی‌پی‌های پاسخ داده:" else "Resolved IPs:"} ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            res.resolvedIps.take(2).forEach { ip ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = ip,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Anti-Sanction or TTL badge
                        if (res.isAntiSanctionVerified) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AppleGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = AppleGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isPersian) "عبور از تحریم" else "Bypass OK",
                                        color = AppleGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (res.ttlSeconds != null) {
                            Text(
                                text = "TTL ${res.ttlSeconds}s",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
