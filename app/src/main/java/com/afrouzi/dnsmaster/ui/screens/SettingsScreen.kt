package com.afrouzi.dnsmaster.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
    modifier: Modifier = Modifier,
    isPersian: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentLanguage by repository.languageFlow.collectAsState(initial = DnsRepository.currentLanguage.value)
    val currentTheme by repository.themeFlow.collectAsState(initial = "dark")
    val autoConnectBoot by repository.autoConnectBootFlow.collectAsState(initial = false)

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
