package com.kapdroid.pomtom.platform

import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class FocusModeController(private val activityHolder: ActivityHolder) {

    private val _state = MutableStateFlow(FocusMode.DISABLED)
    actual val state: StateFlow<FocusMode> = _state.asStateFlow()

    actual fun enable() {
        val activity = activityHolder.activity ?: return
        activity.runOnUiThread {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        _state.value = FocusMode.ENABLED
    }

    actual fun disable() {
        val activity = activityHolder.activity ?: return
        activity.runOnUiThread {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        }
        _state.value = FocusMode.DISABLED
    }
}
