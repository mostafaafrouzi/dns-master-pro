package com.afrouzi.dnsmaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.theme.NeonCyan
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
            .padding(16.dp)
    ) {
        Text(
            text = if (isPersian) "تنظیمات" else "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Preferences
        Text(
            text = if (isPersian) "ترجیحات کاربری" else "Preferences",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Language Setting Row
                SettingsRow(
                    icon = Icons.Default.Translate,
                    title = if (isPersian) "زبان برنامه" else "Language",
                    subtitle = if (currentLanguage == "fa") "فارسی (Persian)" else "English"
                ) {
                    Row {
                        FilterChip(
                            selected = currentLanguage == "fa",
                            onClick = { coroutineScope.launch { repository.setLanguage("fa") } },
                            label = { Text("فارسی", fontSize = 12.sp) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = currentLanguage == "en",
                            onClick = { coroutineScope.launch { repository.setLanguage("en") } },
                            label = { Text("English", fontSize = 12.sp) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Theme Setting Row
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    title = if (isPersian) "تم ظاهری" else "Theme",
                    subtitle = when (currentTheme) {
                        "light" -> if (isPersian) "روشن" else "Light"
                        "dark" -> if (isPersian) "تیره نئونی" else "Dark Neon"
                        else -> if (isPersian) "هماهنگ با سیستم" else "System"
                    }
                ) {
                    Row {
                        FilterChip(
                            selected = currentTheme == "dark",
                            onClick = { coroutineScope.launch { repository.setTheme("dark") } },
                            label = { Text(if (isPersian) "تیره" else "Dark", fontSize = 12.sp) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = currentTheme == "light",
                            onClick = { coroutineScope.launch { repository.setTheme("light") } },
                            label = { Text(if (isPersian) "روشن" else "Light", fontSize = 12.sp) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Auto-connect on Boot Row
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPersian) "اتصال خودکار پس از بوت" else "Auto-Connect on Boot",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isPersian)
                                    "اتصال فوری DNS هنگام روشن شدن دستگاه"
                                else
                                    "Start DNS service when device restarts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = autoConnectBoot,
                        onCheckedChange = { checked ->
                            coroutineScope.launch { repository.setAutoConnectBoot(checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: About & Architecture
        Text(
            text = if (isPersian) "درباره برنامه و راهنما" else "About & Quick Guide",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersian) "دی‌ان‌اس مستر پرو" else "DNS Master Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPersian) "نسخه ۱.۰.۰ (توسعه‌یافته با Jetpack Compose)" else "Version 1.0.0 (Jetpack Compose)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isPersian)
                        "این اپلیکیشن با استفاده از تکنولوژی مسیریابی اختصاصی DNS (DNS-Only 10.0.0.2/32) بدون کاهش حتی ۱ درصد از سرعت اینترنت شما، استعلام‌های نام دامنه را به سرورهای ایمن یا ضدتحریم ارسال می‌کند."
                    else
                        "Engineered with DNS-only tunneling architecture (10.0.0.2/32) ensuring zero internet speed degradation while redirecting name resolution to ultra-fast and anti-sanction resolvers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                // Quick Settings Tile Tip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian)
                            "می‌توانید کلید میانبر برنامه را به منوی اعلان‌های سریع بالای گوشی (Quick Settings Tile) اضافه کنید."
                        else
                            "You can add the DNS Master Quick Tile to your Android notification drawer for one-tap control.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        content()
    }
}
