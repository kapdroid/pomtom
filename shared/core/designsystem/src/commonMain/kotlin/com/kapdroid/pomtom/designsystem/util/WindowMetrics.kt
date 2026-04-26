package com.kapdroid.pomtom.designsystem.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Snapshot of the current container's dimensions, classified into the breakpoints
 * Pomtom cares about. Used by every screen that needs to behave differently in landscape
 * (split layouts) versus portrait (stacked layouts), and by list-style screens that need
 * to constrain their content width on wide phones / tablets.
 *
 * `isLandscape` is the **container's** orientation, not the device's — `BoxWithConstraints`
 * gives us the actually-available space, which Compose Multiplatform exposes uniformly
 * across Android and iOS without having to reach for platform configuration APIs.
 *
 * The breakpoints (compact ≤ 600 dp, medium ≤ 840 dp, expanded > 840 dp) mirror the
 * Material 3 WindowSizeClass classes — chosen so anyone familiar with the standard
 * Android adaptive guidance immediately recognizes what they mean.
 */
@Immutable
data class WindowMetrics(
    val widthDp: Dp,
    val heightDp: Dp,
    val isLandscape: Boolean,
    val widthClass: WidthSizeClass,
)

enum class WidthSizeClass { COMPACT, MEDIUM, EXPANDED }

/**
 * Reads the available container size and offers it as [WindowMetrics] to its content.
 * Use this once near the root of a screen, then branch your layout off the metrics:
 *
 * ```
 * WithWindowMetrics { m ->
 *     if (m.isLandscape) RowLayout() else ColumnLayout()
 * }
 * ```
 *
 * The block runs inside a `BoxWithConstraints` filling the parent — your screen's root
 * composable should consume it directly (do not wrap in another layout that constrains
 * differently, or the metrics won't match what the user sees).
 */
@Composable
fun WithWindowMetrics(content: @Composable (WindowMetrics) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val metrics = WindowMetrics(
            widthDp = w,
            heightDp = h,
            isLandscape = w > h,
            widthClass = when {
                w < 600.dp -> WidthSizeClass.COMPACT
                w < 840.dp -> WidthSizeClass.MEDIUM
                else -> WidthSizeClass.EXPANDED
            },
        )
        content(metrics)
    }
}

/**
 * Soft cap on horizontal content width for list/form screens (Settings, Goals, Stats,
 * History, AudioMixer, NewGoal). Long lines of body text become hard to read past about
 * 70 characters; on a 10″ tablet in landscape that's reached at ~720 dp. The cap +
 * centering gives the screen comfortable margins without the content stretching to the
 * device edges.
 *
 * No-op on compact widths (phones in portrait), so portrait layouts are unchanged.
 *
 * Use as the outermost modifier on a centered `Box` wrapping the screen's `LazyColumn`.
 */
@Composable
fun centeredContent(
    maxWidth: Dp = 720.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxSize()) {
            content()
        }
    }
}
