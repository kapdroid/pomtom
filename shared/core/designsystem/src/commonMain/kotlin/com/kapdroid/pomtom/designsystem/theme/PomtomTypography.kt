package com.kapdroid.pomtom.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class PomtomTypography(
    val display: TextStyle,
    val titleSerif: TextStyle,
    val titleSans: TextStyle,
    val body: TextStyle,
    val bodyEmphasis: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val mono: TextStyle,
    val timer: TextStyle,
)

val LocalPomtomTypography = staticCompositionLocalOf<PomtomTypography> {
    error("PomtomTypography not provided. Wrap your composable in PomtomTheme.")
}

internal fun defaultPomtomTypography(): PomtomTypography {
    val sans = FontFamily.SansSerif
    val serif = FontFamily.Serif
    val mono = FontFamily.Monospace
    return PomtomTypography(
        display = TextStyle(
            fontFamily = serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 44.sp,
            lineHeight = 50.sp,
            letterSpacing = (-0.5).sp,
        ),
        titleSerif = TextStyle(
            fontFamily = serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.3).sp,
        ),
        titleSans = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.1).sp,
        ),
        body = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        bodyEmphasis = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        label = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        ),
        caption = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.6.sp,
        ),
        mono = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        timer = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Medium,
            fontSize = 56.sp,
            lineHeight = 60.sp,
            letterSpacing = (-1).sp,
        ),
    )
}

internal fun PomtomTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = display,
    displayMedium = display.copy(fontSize = 40.sp, lineHeight = 46.sp),
    displaySmall = titleSerif.copy(fontSize = 32.sp, lineHeight = 38.sp),
    headlineLarge = titleSerif,
    headlineMedium = titleSerif.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = titleSans.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = titleSans.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = titleSans,
    titleSmall = label.copy(fontSize = 14.sp),
    bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = body,
    bodySmall = body.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = label.copy(fontSize = 14.sp),
    labelMedium = label,
    labelSmall = caption,
)
