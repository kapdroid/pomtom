package com.kapdroid.pomtom.designsystem.util

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS actual — plain `safeDrawingPadding`. iOS's FocusModeController doesn't hide the
 * status bar (it only sets `idleTimerDisabled = true`), so the inset is already stable
 * throughout a focus session. No need for IgnoringVisibility tricks.
 */
@Composable
actual fun stableSafeAreaPadding(): Modifier = Modifier.safeDrawingPadding()
