package com.kapdroid.pomtom.designsystem.util

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Android actual — locks the inset to "as if status + nav bars were visible" plus the
 * physical display cutout. When `FocusModeController.enable()` hides the bars, the
 * inset doesn't shrink, so layouts that depend on it (StrictScreen) don't shift.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun stableSafeAreaPadding(): Modifier {
    val insets = WindowInsets.statusBarsIgnoringVisibility
        .union(WindowInsets.navigationBarsIgnoringVisibility)
        .union(WindowInsets.displayCutout)
    return Modifier.windowInsetsPadding(insets)
}
