package com.kapdroid.pomtom.designsystem.components

import androidx.compose.runtime.Composable

/**
 * iOS actual — no-op. There's no system back gesture inside our CMP navigation setup
 * (the iOS app embeds the Compose UIViewController as full-screen, no UIKit-style
 * navigation chevron or swipe-from-edge to pop). The in-screen hold-to-exit
 * affordance remains the canonical way out, matching the lockdown contract.
 */
@Composable
actual fun BackInterceptor(enabled: Boolean) {
    // no-op
}
