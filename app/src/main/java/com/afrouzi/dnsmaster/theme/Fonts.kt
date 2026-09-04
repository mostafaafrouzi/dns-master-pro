package com.afrouzi.dnsmaster.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.afrouzi.dnsmaster.R

/**
 * IRANSansX Eco font mapping:
 * Regular (400) and Bold (700) are mapped to all intermediate weights
 * so Compose never synthesizes a fake outline or jagged Persian glyphs.
 */
private fun ecoFamily(regular: Int, bold: Int): FontFamily = FontFamily(
    Font(regular, FontWeight.Thin),
    Font(regular, FontWeight.ExtraLight),
    Font(regular, FontWeight.Light),
    Font(regular, FontWeight.Normal),
    Font(regular, FontWeight.Medium),
    Font(bold, FontWeight.SemiBold),
    Font(bold, FontWeight.Bold),
    Font(bold, FontWeight.ExtraBold),
    Font(bold, FontWeight.Black),
)

val IranSansX = ecoFamily(R.font.iran_sans_x_regular, R.font.iran_sans_x_bold)
val IranSansXFaNum = ecoFamily(R.font.iran_sans_x_fanum_regular, R.font.iran_sans_x_fanum_bold)

private val PersianLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

@Suppress("DEPRECATION")
private val NoFontPadding = PlatformTextStyle(includeFontPadding = false)

private fun TextStyle.withAppFont(family: FontFamily, weight: FontWeight): TextStyle = copy(
    fontFamily = family,
    fontWeight = weight,
    letterSpacing = 0.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = PersianLineHeight,
)

fun persianTypography(family: FontFamily = IranSansX): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 34.sp,
            lineHeight = 44.sp,
        ),
        displayMedium = base.displayMedium.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 28.sp,
            lineHeight = 36.sp,
        ),
        displaySmall = base.displaySmall.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),
        headlineLarge = base.headlineLarge.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 26.sp,
            lineHeight = 34.sp,
        ),
        headlineMedium = base.headlineMedium.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 22.sp,
            lineHeight = 30.sp,
        ),
        headlineSmall = base.headlineSmall.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 20.sp,
            lineHeight = 28.sp,
        ),
        titleLarge = base.titleLarge.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 18.sp,
            lineHeight = 26.sp,
        ),
        titleMedium = base.titleMedium.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        titleSmall = base.titleSmall.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = base.bodyLarge.withAppFont(family, FontWeight.Normal).copy(
            fontSize = 16.sp,
            lineHeight = 28.sp,
        ),
        bodyMedium = base.bodyMedium.withAppFont(family, FontWeight.Normal).copy(
            fontSize = 15.sp,
            lineHeight = 26.sp,
        ),
        bodySmall = base.bodySmall.withAppFont(family, FontWeight.Normal).copy(
            fontSize = 13.sp,
            lineHeight = 22.sp,
        ),
        labelLarge = base.labelLarge.withAppFont(family, FontWeight.Bold).copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelMedium = base.labelMedium.withAppFont(family, FontWeight.Normal).copy(
            fontSize = 12.sp,
            lineHeight = 18.sp,
        ),
        labelSmall = base.labelSmall.withAppFont(family, FontWeight.Normal).copy(
            fontSize = 11.sp,
            lineHeight = 16.sp,
        ),
    )
}
