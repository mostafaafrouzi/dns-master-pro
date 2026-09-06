package com.afrouzi.dnsmaster.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.BuildConfig
import com.afrouzi.dnsmaster.R
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.theme.*
import com.afrouzi.dnsmaster.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: DnsRepository,
    onNavigateToSplitTunnel: () -> Unit = {},
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentLanguage by repository.languageFlow.collectAsState(initial = DnsRepository.currentLanguage.value)
    val currentTheme by repository.themeFlow.collectAsState(initial = "dark")
    val autoConnectBoot by repository.autoConnectBootFlow.collectAsState(initial = false)
    val dohEnabled by repository.dohEnabledFlow.collectAsState(initial = false)
    val localCacheEnabled by repository.localCacheEnabledFlow.collectAsState(initial = true)
    val autoDisconnectMinutes by repository.autoDisconnectMinutesFlow.collectAsState(initial = 0)
    val splitTunnelMode by repository.splitTunnelModeFlow.collectAsState(initial = com.afrouzi.dnsmaster.model.SplitTunnelMode.ALL_APPS)
    val splitTunnelPackages by repository.splitTunnelPackagesFlow.collectAsState(initial = emptySet())

    val lifecycleOwner = LocalLifecycleOwner.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    var isIgnoringBattery by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            } else {
                true
            }
        )
    }
    var areNotificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isIgnoringBattery = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                }
                areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        areNotificationsEnabled = isGranted
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // iOS Large Navigation Title
        Text(
            text = if (isPersian) "تنظیمات" else "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: General Preferences
        SectionHeader(text = if (isPersian) "تنظیمات عمومی" else "GENERAL PREFERENCES")

        Spacer(modifier = Modifier.height(8.dp))

        IosGroupedCard(cornerRadius = 16.dp) {
            // Language Control
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IosIconBadge(icon = Icons.Default.Language, color = AppleBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "زبان برنامه" else "Language",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPersian) "فارسی / English" else "English / Persian",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                IosSegmentedControl(
                    items = listOf("fa", "en"),
                    selectedItem = currentLanguage,
                    onItemSelected = { lang ->
                        coroutineScope.launch { repository.setLanguage(lang) }
                    },
                    itemLabel = {
                        if (it == "fa") (if (isPersian) "فارسی (IranSansX)" else "Persian (فارسی)")
                        else "English (US)"
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Theme Control
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IosIconBadge(
                        icon = if (currentTheme == "dark") Icons.Default.DarkMode else Icons.Default.LightMode,
                        color = AppleIndigo
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "پوسته ظاهری" else "Appearance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPersian) "طراحی مینیمال تاریک / روشن" else "Dark / Light minimalist theme",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                IosSegmentedControl(
                    items = listOf("dark", "light"),
                    selectedItem = currentTheme,
                    onItemSelected = { theme ->
                        coroutineScope.launch { repository.setTheme(theme) }
                    },
                    itemLabel = {
                        if (it == "dark") (if (isPersian) "حالت تاریک (Dark)" else "Dark Mode")
                        else (if (isPersian) "حالت روشن (Light)" else "Light Mode")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Boot Auto-Connect
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IosIconBadge(icon = Icons.Default.PowerSettingsNew, color = AppleGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "اتصال خودکار پس از روشن شدن" else "Connect on Boot",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian) "فعال‌سازی خودکار سرویس DNS با بالا آمدن گوشی" else "Auto-start VPN when device boots",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IosSwitch(
                    checked = autoConnectBoot,
                    onCheckedChange = { checked ->
                        coroutineScope.launch { repository.setAutoConnectBoot(checked) }
                    }
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Connection Notification Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !areNotificationsEnabled) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            try {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (ignored: Exception) {}
                            }
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IosIconBadge(
                        icon = Icons.Default.Notifications,
                        color = if (areNotificationsEnabled) ApplePurple else AppleOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "اعلان وضعیت اتصال" else "Connection Notification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (areNotificationsEnabled) {
                                if (isPersian) "فعال (نمایش در نوار اعلان‌ها)" else "Active (Visible in drawer)"
                            } else {
                                if (isPersian) "غیرفعال (برای فعال‌سازی ضربه بزنید)" else "Disabled (Tap to enable)"
                            },
                            fontSize = 12.sp,
                            color = if (areNotificationsEnabled) AppleGreen else AppleOrange
                        )
                    }
                }

                Icon(
                    imageVector = if (isPersian) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section: Advanced Protection & Engine
        SectionHeader(text = if (isPersian) "امکانات پیشرفته و بهینه‌سازی" else "ADVANCED PROTECTION & ENGINE")

        Spacer(modifier = Modifier.height(8.dp))

        IosGroupedCard(cornerRadius = 16.dp) {
            // DNS over HTTPS (DoH RFC 8484)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IosIconBadge(icon = Icons.Default.Lock, color = AppleBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "دی‌ان‌اس رمزنگاری‌شده (DoH)" else "DNS over HTTPS (DoH)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian)
                                "رمزنگاری درخواست‌ها از طریق پروتکل امن HTTPS (RFC 8484) جهت افزایش امنیت و حریم خصوصی"
                            else
                                "Encrypt queries via HTTPS (RFC 8484) to defeat ISP tampering",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IosSwitch(
                    checked = dohEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch { repository.setDohEnabled(checked) }
                    }
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Local DNS Cache
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IosIconBadge(icon = Icons.Default.Bolt, color = AppleGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "کش حافظه محلی (Local Cache)" else "Local In-Memory Cache",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian)
                                "پاسخ‌دهی آنی زیر ۱ میلی‌ثانیه به دامنه‌های مکرر با انقضای خودکار TTL"
                            else
                                "Resolve repeated queries in <1ms with automatic TTL caching",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IosSwitch(
                    checked = localCacheEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch { repository.setLocalCacheEnabled(checked) }
                    }
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Auto-Disconnect Timer
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IosIconBadge(icon = Icons.Default.Timer, color = AppleOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "تایمر قطع خودکار اتصال" else "Auto-Disconnect Timer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPersian)
                                "خاموش شدن هوشمند VPN پس از اتمام زمان مشخص‌شده"
                            else
                                "Automatically stop VPN after the selected duration",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val timerOptions = listOf(0, 15, 30, 60, 120)
                IosSegmentedControl(
                    items = timerOptions,
                    selectedItem = autoDisconnectMinutes,
                    onItemSelected = { mins ->
                        coroutineScope.launch { repository.setAutoDisconnectMinutes(mins) }
                    },
                    itemLabel = { mins ->
                        when (mins) {
                            0 -> if (isPersian) "غیرفعال" else "Off"
                            15 -> if (isPersian) "۱۵ دقیقه" else "15m"
                            30 -> if (isPersian) "۳۰ دقیقه" else "30m"
                            60 -> if (isPersian) "۱ ساعت" else "1h"
                            120 -> if (isPersian) "۲ ساعت" else "2h"
                            else -> "${mins}m"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Split Tunneling Navigation Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSplitTunnel() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IosIconBadge(icon = Icons.AutoMirrored.Filled.AltRoute, color = AppleIndigo)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "تفکیک ترافیک برنامه‌ها (Split Tunneling)" else "Per-App Split Tunneling",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val modeDesc = when (splitTunnelMode) {
                            com.afrouzi.dnsmaster.model.SplitTunnelMode.ALL_APPS ->
                                if (isPersian) "همه برنامه‌ها (پیش‌فرض)" else "All apps protected"
                            com.afrouzi.dnsmaster.model.SplitTunnelMode.BYPASS_SELECTED ->
                                if (isPersian) "${splitTunnelPackages.size} برنامه استثنا شده" else "${splitTunnelPackages.size} apps bypassed"
                            com.afrouzi.dnsmaster.model.SplitTunnelMode.ONLY_SELECTED ->
                                if (isPersian) "${splitTunnelPackages.size} برنامه انتخابی" else "${splitTunnelPackages.size} apps routed"
                        }
                        Text(
                            text = modeDesc,
                            fontSize = 12.sp,
                            color = AppleBlue
                        )
                    }
                }

                Icon(
                    imageVector = if (isPersian) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Battery Optimization Exemption Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (ignored: Exception) {}
                            }
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IosIconBadge(
                        icon = Icons.Default.BatterySaver,
                        color = if (isIgnoringBattery) AppleGreen else AppleOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "استثنا از محدودیت باتری (پس‌زمینه)" else "Battery Optimization Exemption",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isIgnoringBattery) {
                                if (isPersian) "نامحدود (سرویس در پس‌زمینه پایدار است)" else "Unrestricted (Safe from killing)"
                            } else {
                                if (isPersian) "دارای محدودیت (برای بهینه‌سازی ضربه بزنید)" else "Restricted (Tap to exempt)"
                            },
                            fontSize = 12.sp,
                            color = if (isIgnoringBattery) AppleGreen else AppleOrange
                        )
                    }
                }

                Icon(
                    imageVector = if (isPersian) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: Architecture & Security
        SectionHeader(text = if (isPersian) "امنیت و معماری شبکه" else "SECURITY & ARCHITECTURE")

        Spacer(modifier = Modifier.height(8.dp))

        IosGroupedCard(cornerRadius = 16.dp) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                IosIconBadge(icon = Icons.Default.Security, color = AppleTeal)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isPersian) "مسیریابی امن و بومی DNS-Only" else "DNS-Only Local Routing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPersian)
                            "ترافیک دانلود، وبگردی، بازی‌ها و داده‌های خصوصی شما هرگز از هیچ سرور واسطی عبور داده نمی‌شود. صرفاً کوئری‌های استاندارد پورت ۵۳ به سرور انتخابی شما ارسال می‌شوند."
                        else
                            "Your personal traffic, downloads, and browsing data are NEVER proxied through remote servers. Only UDP DNS queries are redirected locally to your chosen provider.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            IosHairlineDivider(startIndent = 16.dp)

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                IosIconBadge(icon = Icons.Default.Bolt, color = AppleOrange)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isPersian) "کاشی اعلان‌های سریع (Quick Tile)" else "Quick Settings Tile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPersian)
                            "می‌توانید کلید میانبر DNS Master را به منوی تنظیمات سریع اعلان‌های بالای صفحه اضافه کرده و بدون باز کردن برنامه، اتصال را روشن یا خاموش کنید."
                        else
                            "Add the DNS Master Quick Tile to your Android quick settings drawer for convenient one-tap control from any app.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 3: App Overview Card
        SectionHeader(text = if (isPersian) "درباره نرم‌افزار" else "ABOUT APPLICATION")

        Spacer(modifier = Modifier.height(8.dp))

        IosGroupedCard(cornerRadius = 16.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IosIconBadge(icon = Icons.Default.Dns, color = AppleBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "دی‌ان‌اس مستر پرو" else "DNS Master Pro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "نسخه ${BuildConfig.VERSION_NAME} • پایداری بالا و معماری مدرن",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppleBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            color = AppleBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isPersian)
                        "دی‌ان‌اس مستر یک ابزار متن‌باز، مدرن و فوق‌العاده سریع برای تغییر DNS بدون نیاز به روت است. این برنامه با تفکیک ترافیک و مانیتورینگ دقیق تاخیر، امنیت حریم خصوصی و پایداری اینترنت شما را تضمین می‌کند."
                    else
                        "DNS Master is an open-source, modern, and ultra-fast DNS changer requiring no root. It protects your privacy, reduces latency, and bypasses restrictive filters cleanly.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 4: Developer Profile (Matching MyCard Profile & User Request)
        SectionHeader(text = if (isPersian) "توسعه‌دهنده" else "DEVELOPER")

        Spacer(modifier = Modifier.height(8.dp))

        IosGroupedCard(cornerRadius = 18.dp) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Developer Header & Bio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AppleBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isPersian) "مصطفی افروزی" else "Mostafa Afrouzi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian)
                                "طراحی و توسعه وب، اپلیکیشن و بازاریابی دیجیتال"
                            else
                                "Web & Mobile Developer • Digital Marketer",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isPersian)
                        "من مصطفی افروزی هستم؛ طراح و توسعه‌دهنده وب و اپلیکیشن با تمرکز بر سئو، تبلیغات گوگل و اتوماسیون بازاریابی. پروژه‌ها را با نگاه تجاری، تجربه‌کاربری تمیز و زیرساخت فنی پایدار اجرا می‌کنم."
                    else
                        "I'm Mostafa Afrouzi, a web & mobile application developer focused on SEO, Google Ads, and marketing automation. I build high-performance products with clean UX and rock-solid architecture.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                IosHairlineDivider()

                // Link 1: Website with language-specific UTM
                DeveloperLinkRow(
                    drawableId = R.drawable.ic_website,
                    title = if (isPersian) "وب‌سایت رسمی (afrouzi.ir)" else "Official Website (afrouzi.ir/en)",
                    subtitle = if (isPersian) "مقالات، خدمات و نمونه‌کارها" else "Articles, services & portfolio",
                    isPersian = isPersian,
                    onClick = {
                        val url = if (isPersian)
                            "https://afrouzi.ir/?utm_source=dnsmaster&utm_medium=about_screen&utm_campaign=dnsmaster"
                        else
                            "https://afrouzi.ir/en/?utm_source=dnsmaster&utm_medium=about_screen&utm_campaign=dnsmaster"
                        openUrl(url)
                    }
                )

                IosHairlineDivider(startIndent = 42.dp)

                // Link 2: GitHub Profile
                DeveloperLinkRow(
                    drawableId = R.drawable.ic_github,
                    title = if (isPersian) "گیت‌هاب (GitHub)" else "GitHub Profile",
                    subtitle = "github.com/mostafaafrouzi",
                    isPersian = isPersian,
                    onClick = {
                        openUrl("https://github.com/mostafaafrouzi")
                    }
                )

                IosHairlineDivider(startIndent = 42.dp)

                // Link 3: LinkedIn Profile
                DeveloperLinkRow(
                    drawableId = R.drawable.ic_linkedin,
                    title = if (isPersian) "لینکدین (LinkedIn)" else "LinkedIn Profile",
                    subtitle = "linkedin.com/in/mostafaafrouzi",
                    isPersian = isPersian,
                    onClick = {
                        openUrl("https://linkedin.com/in/mostafaafrouzi")
                    }
                )

                IosHairlineDivider(startIndent = 42.dp)

                // Link 4: CafeBazaar Apps (Requested)
                DeveloperLinkRow(
                    drawableId = R.drawable.ic_cafebazaar,
                    title = if (isPersian) "برنامه‌های دیگر در بازار" else "Other apps on CafeBazaar",
                    subtitle = "cafebazaar.ir/developer/057657612999",
                    isPersian = isPersian,
                    onClick = {
                        openUrl("https://cafebazaar.ir/developer/057657612999")
                    }
                )

                IosHairlineDivider(startIndent = 42.dp)

                // Link 5: Myket Apps (Requested)
                DeveloperLinkRow(
                    drawableId = R.drawable.ic_myket,
                    title = if (isPersian) "برنامه‌های دیگر من در مایکت" else "Other apps on Myket",
                    subtitle = "myket.ir/developer/dev-102174",
                    isPersian = isPersian,
                    onClick = {
                        openUrl("https://myket.ir/developer/dev-102174")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Open Source Footer Note
        Text(
            text = if (isPersian)
                "این پروژه کاملاً متن‌باز و رایگان است و مشارکت در توسعه آن آزاد می‌باشد."
            else
                "This project is completely open-source. Contributions and feedback are warmly welcomed.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun DeveloperLinkRow(
    drawableId: Int,
    title: String,
    subtitle: String,
    isPersian: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(drawableId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = if (isPersian) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
private fun IosIconBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )
    }
}
