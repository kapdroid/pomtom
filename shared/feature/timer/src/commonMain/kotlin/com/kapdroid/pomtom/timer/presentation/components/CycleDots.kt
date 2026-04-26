package com.kapdroid.pomtom.timer.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.timer.presentation.CycleDot

@Composable
fun CycleDotsRow(dots: List<CycleDot>, modifier: Modifier = Modifier) {
    if (dots.isEmpty()) return
    val colors = PomtomTheme.colors
    val transition = rememberInfiniteTransition(label = "cycle-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1100), repeatMode = RepeatMode.Reverse),
        label = "pulse",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        dots.forEachIndexed { index, dot ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(28.dp)
                        .background(
                            color = if (dot.isCompleted || dot.isActive) colors.amber.copy(alpha = 0.7f) else colors.ink3.copy(alpha = 0.3f),
                        ),
                )
            }
            val size = when {
                dot.isActive -> (16 * pulse).dp
                else -> 12.dp
            }
            Spacer(Modifier.width(if (index == 0) 0.dp else 6.dp))
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        brush = when {
                            dot.isActive -> Brush.linearGradient(listOf(colors.amber, colors.ember))
                            dot.isCompleted -> Brush.linearGradient(listOf(colors.amber.copy(alpha = 0.55f), colors.ember.copy(alpha = 0.55f)))
                            else -> Brush.linearGradient(listOf(colors.ink3.copy(alpha = 0.4f), colors.ink3.copy(alpha = 0.4f)))
                        },
                    ),
            )
            Spacer(Modifier.width(if (index == dots.size - 1) 0.dp else 6.dp))
        }
    }
}
