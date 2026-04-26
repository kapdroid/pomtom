package com.kapdroid.pomtom.platform

import kotlinx.coroutines.flow.StateFlow

expect class FocusModeController {
    val state: StateFlow<FocusMode>
    fun enable()
    fun disable()
}
