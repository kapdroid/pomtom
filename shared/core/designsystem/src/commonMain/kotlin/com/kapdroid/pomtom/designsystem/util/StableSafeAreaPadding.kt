package com.kapdroid.pomtom.designsystem.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A safe-area inset that does NOT shrink when the system bars hide. Used by Pomtom's
 * StrictScreen, where `FocusModeController.enable()` (Android) hides the system bars
 * shortly after composition — a plain `safeDrawingPadding()` would shrink at that
 * moment and visibly shift the layout. This modifier returns the same padding before
 * and after the hide, so the layout stays still.
 *
 * Platform behavior:
 *  - **Android** — combines `WindowInsets.statusBarsIgnoringVisibility +
 *    navigationBarsIgnoringVisibility + displayCutout`. The IgnoringVisibility
 *    variants always report the bar size as if it were visible, so the inset stays
 *    constant whether the bars are shown or hidden.
 *  - **iOS** — falls back to `safeDrawingPadding()`. iOS's FocusModeController only
 *    disables the idle timer; it doesn't hide the status bar. The inset is already
 *    stable throughout a session, so no special handling is needed.
 */
@Composable
expect fun stableSafeAreaPadding(): Modifier
