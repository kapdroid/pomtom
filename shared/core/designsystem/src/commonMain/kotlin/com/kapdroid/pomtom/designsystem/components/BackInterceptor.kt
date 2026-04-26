package com.kapdroid.pomtom.designsystem.components

import androidx.compose.runtime.Composable

/**
 * Consumes the platform's system-back gesture while [enabled] is true, preventing the
 * usual navigation pop. Used by Pomtom's StrictScreen to enforce hold-to-exit as the
 * only way out of an active focus session.
 *
 * Platform behavior:
 *  - **Android** — wraps `androidx.activity.compose.BackHandler`. While enabled, the
 *    system back press / gesture is intercepted and discarded. The user is forced to
 *    use the in-screen "Hold to emergency exit" gesture.
 *  - **iOS** — no-op. iOS doesn't have an equivalent system-back gesture inside our
 *    Compose-Multiplatform navigation setup (no swipe-from-edge to pop a NavHost
 *    composable, no system back chevron). The hold-to-exit affordance is the only
 *    visible exit on iOS too, so the lockdown contract is preserved.
 *
 * Toggle [enabled] off (e.g. when the session is null or in a break phase) to restore
 * normal back behavior.
 */
@Composable
expect fun BackInterceptor(enabled: Boolean = true)
