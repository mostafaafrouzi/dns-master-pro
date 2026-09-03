package com.afrouzi.dnsmaster.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    tertiary = NeonEmerald,
    onTertiary = Color.White,
    background = SlateDarkBackground,
    onBackground = TextWhitePrimary,
    surface = SlateDarkSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = TextWhiteSecondary,
    outline = SlateDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    tertiary = NeonEmerald,
    onTertiary = Color.White,
    background = SlateLightBackground,
    onBackground = TextDarkPrimary,
    surface = SlateLightSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = SlateLightBorder
)

@Composable
fun DnsMasterTheme(
    themePreference: String = "dark",
    content: @Composable () -> Unit,
) {
    val isDark = when (themePreference) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
