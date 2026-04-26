package com.kapdroid.pomtom.common

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object Logger {
    private var initialized = false

    fun install(debug: Boolean = true) {
        if (initialized) return
        if (debug) Napier.base(DebugAntilog())
        initialized = true
    }

    fun d(tag: String? = null, message: () -> String) = Napier.d(tag = tag, message = message)
    fun i(tag: String? = null, message: () -> String) = Napier.i(tag = tag, message = message)
    fun w(tag: String? = null, throwable: Throwable? = null, message: () -> String) =
        Napier.w(tag = tag, throwable = throwable, message = message)
    fun e(tag: String? = null, throwable: Throwable? = null, message: () -> String) =
        Napier.e(tag = tag, throwable = throwable, message = message)
}
