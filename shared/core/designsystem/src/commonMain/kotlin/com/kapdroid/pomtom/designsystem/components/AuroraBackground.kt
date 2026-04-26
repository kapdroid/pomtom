package com.kapdroid.pomtom.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = PomtomTheme.colors
    val motion = PomtomTheme.motion
    val wallpaperPath = LocalWallpaperPath.current
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = motion.auroraDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora-phase",
    )
    Box(modifier = modifier.fillMaxSize().background(colors.bg0)) {
        if (wallpaperPath != null) {
            AsyncImage(
                model = wallpaperPath.asFileUri(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (colors.isDark) 0.55f else 0.30f,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.bg0.copy(alpha = if (colors.isDark) 0.45f else 0.55f)),
            )
        }
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val twoPi = (2.0 * kotlin.math.PI).toFloat()

            val c1 = Offset(
                x = w * (0.30f + 0.10f * cos(phase * twoPi)),
                y = h * (0.20f + 0.06f * sin(phase * twoPi)),
            )
            val c2 = Offset(
                x = w * (0.75f + 0.08f * sin(phase * twoPi + 1f)),
                y = h * (0.65f + 0.10f * cos(phase * twoPi + 1f)),
            )
            val c3 = Offset(
                x = w * (0.20f + 0.10f * cos(phase * twoPi + 2.4f)),
                y = h * (0.85f + 0.05f * sin(phase * twoPi + 2.4f)),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.amber.copy(alpha = 0.22f), Color.Transparent),
                    center = c1,
                    radius = w * 0.55f,
                ),
                radius = w * 0.55f,
                center = c1,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.ember.copy(alpha = 0.18f), Color.Transparent),
                    center = c2,
                    radius = w * 0.60f,
                ),
                radius = w * 0.60f,
                center = c2,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.violet.copy(alpha = 0.14f), Color.Transparent),
                    center = c3,
                    radius = w * 0.50f,
                ),
                radius = w * 0.50f,
                center = c3,
            )
        }
        content()
    }
}

private fun String.asFileUri(): String =
    if (startsWith("file:") || startsWith("content:") || startsWith("http")) this
    else "file://$this"
