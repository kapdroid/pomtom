package com.kapdroid.pomtom.designsystem.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import androidx.compose.runtime.Composable

@Composable
fun Modifier.glassCard(
    shape: Shape = PomtomTheme.shapes.card,
    borderAlpha: Float = 0.45f,
    borderWidth: Dp = 1.dp,
): Modifier {
    val colors = PomtomTheme.colors
    return this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.bg2.copy(alpha = if (colors.isDark) 0.78f else 0.95f),
                    colors.bg1.copy(alpha = if (colors.isDark) 0.78f else 0.95f),
                ),
            ),
            shape = shape,
        )
        .border(width = borderWidth, color = colors.ink3.copy(alpha = borderAlpha), shape = shape)
}

@Composable
fun Modifier.softCard(
    shape: Shape = PomtomTheme.shapes.card,
): Modifier {
    val colors = PomtomTheme.colors
    return this
        .clip(shape)
        .background(color = if (colors.isDark) colors.bg2 else colors.surface, shape = shape)
        .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.30f), shape = shape)
}

@Composable
fun accentBrush(): Brush {
    val colors = PomtomTheme.colors
    return Brush.horizontalGradient(listOf(colors.amber, colors.ember))
}

fun softShadowColor(base: Color): Color = base.copy(alpha = 0.18f)
