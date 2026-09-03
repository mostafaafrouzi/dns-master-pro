package com.afrouzi.dnsmaster.ui.main

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.data.repository.DnsRepository
import com.afrouzi.dnsmaster.theme.DnsMasterTheme
import com.afrouzi.dnsmaster.theme.NeonCyan
import com.afrouzi.dnsmaster.ui.screens.*

enum class AppTab {
    HOME,
    SERVERS,
    SPEED_TEST,
    CUSTOM_DNS,
    SETTINGS
}

@Composable
fun MainAppShell() {
    val context = LocalContext.current
    val repository = remember { DnsRepository.getInstance(context) }

    val language by repository.languageFlow.collectAsState(initial = "fa")
    val themePreference by repository.themeFlow.collectAsState(initial = "dark")

    val isPersian = language == "fa"
    val layoutDirection = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr

    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Request notification permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        DnsMasterTheme(themePreference = themePreference) {
            Scaffold(
                bottomBar = {
                    if (currentTab != AppTab.CUSTOM_DNS) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.HOME,
                                onClick = { currentTab = AppTab.HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = {
                                    Text(
                                        text = if (isPersian) "خانه" else "Home",
                                        fontSize = 12.sp,
                                        fontWeight = if (currentTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.SERVERS,
                                onClick = { currentTab = AppTab.SERVERS },
                                icon = { Icon(Icons.Default.Dns, contentDescription = "Servers") },
                                label = {
                                    Text(
                                        text = if (isPersian) "سرورها" else "Servers",
                                        fontSize = 12.sp,
                                        fontWeight = if (currentTab == AppTab.SERVERS) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.SPEED_TEST,
                                onClick = { currentTab = AppTab.SPEED_TEST },
                                icon = { Icon(Icons.Default.Speed, contentDescription = "Speed Test") },
                                label = {
                                    Text(
                                        text = if (isPersian) "تست سرعت" else "Speed Test",
                                        fontSize = 12.sp,
                                        fontWeight = if (currentTab == AppTab.SPEED_TEST) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.SETTINGS,
                                onClick = { currentTab = AppTab.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = {
                                    Text(
                                        text = if (isPersian) "تنظیمات" else "Settings",
                                        fontSize = 12.sp,
                                        fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Crossfade(
                    targetState = currentTab,
                    modifier = Modifier.padding(innerPadding),
                    label = "tab_crossfade"
                ) { tab ->
                    when (tab) {
                        AppTab.HOME -> HomeScreen(
                            repository = repository,
                            onNavigateToServers = { currentTab = AppTab.SERVERS },
                            onNavigateToSpeedTest = { currentTab = AppTab.SPEED_TEST },
                            isPersian = isPersian
                        )
                        AppTab.SERVERS -> DnsListScreen(
                            repository = repository,
                            onNavigateToAddCustom = { currentTab = AppTab.CUSTOM_DNS },
                            isPersian = isPersian
                        )
                        AppTab.SPEED_TEST -> SpeedTestScreen(
                            repository = repository,
                            isPersian = isPersian
                        )
                        AppTab.CUSTOM_DNS -> CustomDnsScreen(
                            repository = repository,
                            onNavigateBack = { currentTab = AppTab.SERVERS },
                            isPersian = isPersian
                        )
                        AppTab.SETTINGS -> SettingsScreen(
                            repository = repository,
                            isPersian = isPersian
                        )
                    }
                }
            }
        }
    }
}
