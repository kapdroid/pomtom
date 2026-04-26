package com.kapdroid.pomtom.designsystem.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android actual — wraps the AndroidX `BackHandler`. The lambda is intentionally empty:
 * we want to *swallow* the back event during a strict session, not act on it. The user
 * must use the in-screen 3-second hold-to-exit instead.
 */
@Composable
actual fun BackInterceptor(enabled: Boolean) {
    BackHandler(enabled = enabled) {
        // intentionally empty — back press consumed and discarded
    }
}
