package com.kapdroid.pomtom.di

import com.kapdroid.pomtom.AndroidContextHolder
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

actual fun KoinApplication.platformKoinConfig() {
    androidContext(AndroidContextHolder.application)
}
