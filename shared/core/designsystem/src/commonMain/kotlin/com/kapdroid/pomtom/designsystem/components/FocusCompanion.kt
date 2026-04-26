package com.kapdroid.pomtom.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapdroid.pomtom.designsystem.resources.Res
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Resource paths for bundled focus-companion Lottie animations. Each constant matches
 * a JSON file shipped at `composeResources/files/companion/` in the designsystem module.
 *
 * Callers should not hard-code these strings elsewhere — go through this object so a
 * filename change is a one-line edit.
 */
object CompanionAssets {
    const val MEDITATING_FOX = "files/companion/meditating_fox.json"
    const val MOON_CAT = "files/companion/moon_cat.json"
    const val PLAYFUL_CAT = "files/companion/playful_cat.json"
}

/**
 * Renders a bundled Lottie companion via Compottie 2.x.
 *
 * The Lottie format itself ships transparency in every frame, so the composable draws
 * directly over whatever background it's placed on — no opaque card behind it.
 *
 * Sizing model:
 *  - The composable reserves a [sizeDp] × [sizeDp] square slot.
 *  - `ContentScale.Fit` scales the source canvas (which may be square OR landscape —
 *    e.g. playful_cat is 1070×456) to fit inside the slot while preserving aspect
 *    ratio. Landscape Lotties end up shorter than [sizeDp] but still use the full
 *    width.
 *
 * Loading model:
 *  - [rememberLottieComposition] is a suspend producer; while it's still loading the
 *    painter has no frames and `Image` paints nothing — that's deliberate so the layout
 *    doesn't flash on first navigation.
 *  - [Compottie.IterateForever] keeps the animation looping for the full session.
 *  - When [paused] is true we drop alpha rather than freezing the animation. A frozen
 *    creature reads as "broken"; a softly dimmed one reads as "resting." The paused
 *    state in the timer is a user-initiated pause, so a gentle visual cue is enough.
 *
 * @param assetPath one of the constants in [CompanionAssets].
 * @param contentDescription accessibility label, or null for a decorative image.
 * @param sizeDp render size — Lottie is vector so any size is crisp.
 * @param paused soft-dim when true (animation keeps playing).
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun FocusCompanion(
    assetPath: String,
    contentDescription: String?,
    sizeDp: Dp = 220.dp,
    paused: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(assetPath) {
        LottieCompositionSpec.JsonString(Res.readBytes(assetPath).decodeToString())
    }
    val painter = rememberLottiePainter(
        composition = composition,
        iterations = Compottie.IterateForever,
    )
    val alpha by animateFloatAsState(
        targetValue = if (paused) 0.45f else 1f,
        label = "companion-alpha",
    )
    val semanticsModifier = contentDescription?.let { label ->
        Modifier.semantics { this.contentDescription = label }
    } ?: Modifier
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .then(semanticsModifier)
            .size(sizeDp)
            .alpha(alpha),
    )
}

/**
 * A small framed preview tile used inside settings pickers. Renders the same Lottie
 * inside a soft cream-card background so the silhouette stays legible against any
 * palette.
 *
 * The tile is a **wide rounded rect** rather than a circle because one of the bundled
 * companion canvases is landscape (playful_cat 1070×456). A circular tile + Fit on
 * landscape content shrinks the actual creature to a small fraction of the visible
 * area. The wider tile lets landscape Lotties use the full width and square Lotties
 * center with modest side-padding.
 *
 * Pass `assetPath = null` to render an "off / none" tile (a long-dash glyph in the
 * same frame).
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun CompanionPreviewTile(
    assetPath: String?,
    widthDp: Dp = 88.dp,
    heightDp: Dp = 56.dp,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(width = widthDp, height = heightDp)
            .clip(shape)
            .background(colors.cream)
            .border(
                width = 1.dp,
                color = colors.ink3.copy(alpha = 0.30f),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (assetPath == null) {
            Text(
                text = "—",
                style = PomtomTheme.typography.titleSerif.copy(
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink3,
            )
        } else {
            val composition by rememberLottieComposition(assetPath) {
                LottieCompositionSpec.JsonString(Res.readBytes(assetPath).decodeToString())
            }
            val painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever,
            )
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
            )
        }
    }
}
