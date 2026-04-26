package com.kapdroid.pomtom.designsystem.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class PomtomMotion(
    val breatheDurationMs: Int = 4_000,
    val pulseDurationMs: Int = 2_000,
    val auroraDurationMs: Int = 30_000,
    val confettiDurationMs: Int = 1_400,
    val standardEasing: () -> AnimationSpec<Float> = { tween(durationMillis = 300, easing = FastOutSlowInEasing) },
    val gentleSpring: () -> AnimationSpec<Float> = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow) },
    val linearLong: (Int) -> AnimationSpec<Float> = { ms -> tween(durationMillis = ms, easing = LinearEasing) },
)

val LocalPomtomMotion = staticCompositionLocalOf<PomtomMotion> {
    error("PomtomMotion not provided. Wrap your composable in PomtomTheme.")
}
