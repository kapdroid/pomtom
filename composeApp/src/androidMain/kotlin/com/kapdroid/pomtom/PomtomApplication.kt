package com.kapdroid.pomtom

import android.app.Application

class PomtomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.application = this
    }
}

object AndroidContextHolder {
    lateinit var application: Application
}
