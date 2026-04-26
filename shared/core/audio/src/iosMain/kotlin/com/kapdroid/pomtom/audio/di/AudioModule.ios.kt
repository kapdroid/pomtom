package com.kapdroid.pomtom.audio.di

import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.audio.AudioProbe
import org.koin.core.module.Module
import org.koin.dsl.module

actual val audioPlatformModule: Module = module {
    single { AudioEngine() }
    single { AudioProbe() }
}
