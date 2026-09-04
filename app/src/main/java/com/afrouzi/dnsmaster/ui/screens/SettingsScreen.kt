package com.afrouzi.dnsmaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.theme.*
import com.afrouzi.dnsmaster.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: DnsRepository,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()

    val currentLanguage by repository.languageFlow.collectAsState(initial = "fa")
    val currentTheme by repository.themeFlow.collectAsState(initial = "dark")
    val autoConnectBoot by repository.autoConnectBootFlow.collectAsState(initial = false)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IosIconBadge(icon = Icons.Default.Translate, color = AppleBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isPersian) "زبان برنامه" else "Language",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                IosSegmentedControl(
                    items = listOf("fa", "en"),
                    selectedItem = currentLanguage,
                    onItemSelected = { lang ->
                        coroutineScope.launch { repository.setLanguage(lang) }
                    },
                    itemLabel = { lang ->
                        if (lang == "fa") "فارسی (Persian)" else "English"
                    }
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Appearance / Theme Control
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IosIconBadge(icon = Icons.Default.DarkMode, color = AppleIndigo)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isPersian) "تم ظاهری" else "Appearance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                IosSegmentedControl(
                    items = listOf("dark", "light", "system"),
                    selectedItem = currentTheme,
                    onItemSelected = { theme ->
                        coroutineScope.launch { repository.setTheme(theme) }
                    },
                    itemLabel = { theme ->
                        when (theme) {
                            "dark" -> if (isPersian) "تیره" else "Dark"
                            "light" -> if (isPersian) "روشن" else "Light"
                            else -> if (isPersian) "خودکار" else "Auto"
                        }
                    }
                )
            }

            IosHairlineDivider(startIndent = 16.dp)

            // Auto-Connect on Boot Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IosIconBadge(icon = Icons.Default.PowerSettingsNew, color = AppleGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "اتصال خودکار پس از بوت" else "Auto-Connect on Boot",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian)
                                "شروع خودکار سرویس DNS پس از راه‌اندازی دستگاه"
                            else
                                "Start DNS service when device restarts",
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
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: Architecture & Security
        SectionHeader(text = if (isPersian) "امنیت و معماری" else "SECURITY & ARCHITECTURE")

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
                        text = if (isPersian) "مسیریابی امن DNS-Only" else "DNS-Only Local Routing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPersian)
                            "ترافیک دانلود، وبگردی و اطلاعات شخصی شما هرگز از هیچ سرور واسطی عبور داده نمی‌شود. فقط درخواست‌های DNS به سرور انتخابی شما هدایت می‌شوند."
                        else
                            "Your personal traffic, downloads, and browsing data are NEVER proxied through remote servers. Only UDP DNS queries are redirected locally.",
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
                IosIconBadge(icon = Icons.Default.Info, color = AppleOrange)
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
                            "می‌توانید کلید میانبر برنامه را به منوی اعلان‌های بالای صفحه اضافه کرده و با یک لمس DNS را قطع یا وصل کنید."
                        else
                            "Add the DNS Master Quick Tile to your Android notification drawer for convenient one-tap control.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 3: About
        SectionHeader(text = if (isPersian) "درباره برنامه" else "ABOUT")

        Spacer(modifier = Modifier.height(8.dp))

        IosGroupedCard(cornerRadius = 16.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IosIconBadge(icon = Icons.Default.Dns, color = AppleBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "دی‌ان‌اس مستر پرو" else "DNS Master Pro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v1.1.0 • Modern iOS Design",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "2026",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
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
