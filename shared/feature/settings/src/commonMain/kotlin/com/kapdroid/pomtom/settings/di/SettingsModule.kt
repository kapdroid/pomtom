package com.kapdroid.pomtom.settings.di

import com.kapdroid.pomtom.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            wallpaperRepository = get(),
            updateSessionConfig = get(),
            toggleStrictMode = get(),
            selectTheme = get(),
            selectWallpaper = get(),
            importWallpaper = get(),
            deleteWallpaper = get(),
            setCompanion = get(),
        )
    }
}
