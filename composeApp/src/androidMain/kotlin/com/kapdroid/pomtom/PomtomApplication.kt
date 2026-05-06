package com.kapdroid.pomtom

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class PomtomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.application = this
        // Wire Napier into Android logcat so debug calls in shared modules
        // (e.g. AudioEngine.playOneShot, FocusAudioController) are visible
        // via `adb logcat | grep Napier`. Without this, every Napier.d/e
        // call is dropped silently.
        Napier.base(DebugAntilog())
    }
}

object AndroidContextHolder {
    lateinit var application: Application
}
