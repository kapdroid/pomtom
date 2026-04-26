package com.kapdroid.pomtom.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PomtomColors(
    val bg0: Color,
    val bg1: Color,
    val bg2: Color,
    val bg3: Color,
    val surface: Color,
    val surface2: Color,
    val cream: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val amber: Color,
    val ember: Color,
    val rose: Color,
    val sage: Color,
    val violet: Color,
    val onAccent: Color,
    val isDark: Boolean,
)

val LocalPomtomColors = staticCompositionLocalOf<PomtomColors> {
    error("PomtomColors not provided. Wrap your composable in PomtomTheme.")
}
