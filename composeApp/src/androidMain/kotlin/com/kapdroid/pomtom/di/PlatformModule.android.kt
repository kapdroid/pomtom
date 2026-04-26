package com.kapdroid.pomtom.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Phase 2 will register: DatabaseDriverFactory, SettingsStore, AudioEngine, FilePicker, FocusModeController.
}
