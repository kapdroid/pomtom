package com.kapdroid.pomtom.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EqIcon(
    color: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val phase1 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 1f else 0.4f,
        animationSpec = infiniteRepeatable(tween(620), RepeatMode.Reverse),
        label = "eq-1",
    )
    val phase2 by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isPlaying) 1f else 0.6f,
        animationSpec = infiniteRepeatable(tween(820), RepeatMode.Reverse),
        label = "eq-2",
    )
    val phase3 by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isPlaying) 1f else 0.5f,
        animationSpec = infiniteRepeatable(tween(560), RepeatMode.Reverse),
        label = "eq-3",
    )
    val phase4 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = if (isPlaying) 1f else 0.45f,
        animationSpec = infiniteRepeatable(tween(720), RepeatMode.Reverse),
        label = "eq-4",
    )
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val barW = w / 7f
        val gap = barW
        val xs = listOf(barW * 0.5f, barW * 0.5f + barW + gap, barW * 0.5f + 2f * (barW + gap), barW * 0.5f + 3f * (barW + gap))
        val phases = listOf(phase1, phase2, phase3, phase4)
        phases.forEachIndexed { idx, p ->
            val barH = h * p
            drawRoundRect(
                color = color,
                topLeft = Offset(xs[idx], h - barH),
                size = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f, barW / 2f),
            )
        }
    }
}
