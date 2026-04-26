package com.kapdroid.pomtom.audio.di

import org.koin.core.module.Module

expect val audioPlatformModule: Module

val audioModule: Module
    get() = audioPlatformModule
