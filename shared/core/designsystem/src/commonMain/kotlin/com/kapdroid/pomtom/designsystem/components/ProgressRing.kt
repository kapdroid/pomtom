package com.kapdroid.pomtom.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ProgressRing(
    progress: Float,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    diameter: Dp = 280.dp,
    strokeWidth: Dp = 14.dp,
    content: @Composable () -> Unit = {},
) {
    val colors = PomtomTheme.colors
    val motion = PomtomTheme.motion
    val safeProgress = progress.coerceIn(0f, 1f)

    val breathTransition = rememberInfiniteTransition(label = "ring-breathe")
    val breath by breathTransition.animateFloat(
        initialValue = if (isRunning) 0.96f else 1f,
        targetValue = if (isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = motion.breatheDurationMs),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring-breath",
    )

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(diameter)) {
            val canvasSize = min(size.width, size.height)
            val stroke = strokeWidth.toPx()
            val ringSize = canvasSize - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = Size(ringSize, ringSize)

            drawArc(
                color = colors.bg2,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = colors.amber.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset((stroke / 2f) - 4f, (stroke / 2f) - 4f),
                size = Size(ringSize + 8f, ringSize + 8f),
                style = Stroke(width = stroke + 8f, cap = StrokeCap.Round),
                alpha = if (isRunning) (breath - 0.96f) * 4f else 0f,
            )

            val sweep = safeProgress * 360f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(colors.amber, colors.ember, colors.amber),
                        center = Offset(canvasSize / 2f, canvasSize / 2f),
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            val ticks = 60
            val tickRadiusOuter = canvasSize / 2f - stroke - 4f
            val tickRadiusInner = tickRadiusOuter - 6f
            val majorRadiusInner = tickRadiusOuter - 10f
            val center = Offset(canvasSize / 2f, canvasSize / 2f)
            for (i in 0 until ticks) {
                val angle = (i * (360f / ticks) - 90f) * (kotlin.math.PI / 180.0).toFloat()
                val major = i % 15 == 0
                val inner = if (major) majorRadiusInner else tickRadiusInner
                val start = Offset(
                    x = center.x + tickRadiusOuter * cos(angle),
                    y = center.y + tickRadiusOuter * sin(angle),
                )
                val end = Offset(
                    x = center.x + inner * cos(angle),
                    y = center.y + inner * sin(angle),
                )
                drawLine(
                    color = if (major) colors.ink2.copy(alpha = 0.5f) else colors.ink3.copy(alpha = 0.25f),
                    start = start,
                    end = end,
                    strokeWidth = if (major) 2f else 1f,
                    cap = StrokeCap.Round,
                )
            }
        }
        // Breathing scale of children
        Box(
            modifier = Modifier.size(diameter * 0.78f),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
        // Halo highlight that travels with the progress endpoint.
        if (isRunning && safeProgress > 0f) {
            Canvas(Modifier.size(diameter)) {
                val canvasSize = min(size.width, size.height)
                val stroke = strokeWidth.toPx()
                rotate(degrees = -90f + safeProgress * 360f) {
                    drawCircle(
                        color = colors.surface.copy(alpha = 0.85f),
                        radius = stroke * 0.42f,
                        center = Offset(canvasSize / 2f, stroke / 2f),
                    )
                    drawCircle(
                        color = colors.amber.copy(alpha = 0.40f),
                        radius = stroke * 0.95f,
                        center = Offset(canvasSize / 2f, stroke / 2f),
                    )
                }
            }
        }
    }
}
