package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.repository.SettingsRepository

class UpdateSessionConfigUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke(config: SessionConfig) = settings.setSessionConfig(config)
}

class ToggleStrictModeUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = settings.setStrictMode(enabled)
}

class SelectThemeUseCase(
    private val settings: SettingsRepository,
    private val eventBus: EventBus,
) {
    suspend operator fun invoke(theme: AppTheme) {
        settings.setTheme(theme)
        eventBus.emit(DomainEvent.ThemeChanged)
    }
}

class SelectWallpaperUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke(wallpaperId: String?) = settings.setWallpaperId(wallpaperId)
}

class CompleteOnboardingUseCase(private val settings: SettingsRepository) {
    suspend operator fun invoke() = settings.setFirstRunComplete(true)
}
