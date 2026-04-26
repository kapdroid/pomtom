package com.kapdroid.pomtom.platform.di

import com.kapdroid.pomtom.platform.FocusModeController
import com.kapdroid.pomtom.platform.Haptics
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformControllerModule: Module = module {
    single { FocusModeController() }
    single { Haptics() }
}
