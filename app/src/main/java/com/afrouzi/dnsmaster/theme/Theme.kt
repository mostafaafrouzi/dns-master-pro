package com.afrouzi.dnsmaster.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppleBlueDark,
    onPrimary = Color.White,
    primaryContainer = AppleBlueDark.copy(alpha = 0.2f),
    onPrimaryContainer = AppleBlueDark,
    secondary = AppleIndigoDark,
    onSecondary = Color.White,
    tertiary = AppleGreenDark,
    onTertiary = Color.White,
    background = IosDarkBackground,
    onBackground = IosDarkTextPrimary,
    surface = IosDarkSurface,
    onSurface = IosDarkTextPrimary,
    surfaceVariant = IosDarkSurfaceVariant,
    onSurfaceVariant = IosDarkTextSecondary,
    outline = IosDarkSeparator
)

private val LightColorScheme = lightColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = AppleBlue.copy(alpha = 0.12f),
    onPrimaryContainer = AppleBlue,
    secondary = AppleIndigo,
    onSecondary = Color.White,
    tertiary = AppleGreen,
    onTertiary = Color.White,
    background = IosLightBackground,
    onBackground = IosLightTextPrimary,
    surface = IosLightSurface,
    onSurface = IosLightTextPrimary,
    surfaceVariant = IosLightSurfaceVariant,
    onSurfaceVariant = IosLightTextSecondary,
    outline = IosLightSeparator
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
