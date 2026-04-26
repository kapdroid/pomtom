package com.kapdroid.pomtom.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class FocusModeController {

    private val _state = MutableStateFlow(FocusMode.DISABLED)
    actual val state: StateFlow<FocusMode> = _state.asStateFlow()

    actual fun enable() {
        runOnMain { UIApplication.sharedApplication.idleTimerDisabled = true }
        _state.value = FocusMode.ENABLED
    }

    actual fun disable() {
        runOnMain { UIApplication.sharedApplication.idleTimerDisabled = false }
        _state.value = FocusMode.DISABLED
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun runOnMain(block: () -> Unit) {
        dispatch_async(dispatch_get_main_queue(), block)
    }
}
