package com.kapdroid.pomtom.timer.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConfettiOverlay(
    pieceCount: Int = 60,
    seed: Long = 42L,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val palette = remember(colors) { listOf(colors.amber, colors.ember, colors.rose, colors.violet, colors.sage) }
    val pieces = remember(pieceCount, seed, palette) {
        val rng = Random(seed)
        List(pieceCount) {
            ConfettiPiece(
                seedX = rng.nextFloat(),
                drift = (rng.nextFloat() - 0.5f) * 0.4f,
                width = 4f + rng.nextFloat() * 6f,
                height = 10f + rng.nextFloat() * 8f,
                color = palette[it % palette.size],
                spin = (rng.nextFloat() * 720f) - 360f,
                cycleSeconds = 2.4f + rng.nextFloat() * 2.0f,
                delaySeconds = rng.nextFloat() * 1.0f,
                wobbleAmplitude = 14f + rng.nextFloat() * 22f,
                wobblePhase = rng.nextFloat() * 6.2832f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val tick by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
        ),
        label = "confetti-tick",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        pieces.forEach { piece ->
            val raw = ((tick * 4.2f) + piece.delaySeconds) % piece.cycleSeconds
            val progress = raw / piece.cycleSeconds
            val y = -piece.height + progress * (h + piece.height * 2f)
            val baseX = piece.seedX * w
            val xDrift = piece.drift * w * progress
            val wobble = sin(progress * 6.2832f * 2f + piece.wobblePhase) * piece.wobbleAmplitude
            val x = baseX + xDrift + wobble
            val rotation = piece.spin * progress
            translate(left = x, top = y) {
                rotate(degrees = rotation, pivot = Offset(piece.width / 2f, piece.height / 2f)) {
                    val rect = RoundRect(
                        left = 0f,
                        top = 0f,
                        right = piece.width,
                        bottom = piece.height,
                        topLeftCornerRadius = CornerRadius(piece.width * 0.4f, piece.height * 0.6f),
                        topRightCornerRadius = CornerRadius(piece.width * 0.6f, piece.height * 0.4f),
                        bottomLeftCornerRadius = CornerRadius(piece.width * 0.6f, piece.height * 0.4f),
                        bottomRightCornerRadius = CornerRadius(piece.width * 0.4f, piece.height * 0.6f),
                    )
                    val path = Path().apply { addRoundRect(rect) }
                    drawPath(path = path, color = piece.color.copy(alpha = 0.85f))
                }
            }
        }
    }
}

private data class ConfettiPiece(
    val seedX: Float,
    val drift: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val spin: Float,
    val cycleSeconds: Float,
    val delaySeconds: Float,
    val wobbleAmplitude: Float,
    val wobblePhase: Float,
)
