package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.domain.repository.SettingsRepository
import com.kapdroid.pomtom.domain.repository.WallpaperRepository

class DeleteWallpaperUseCase(
    private val wallpapers: WallpaperRepository,
    private val settings: SettingsRepository,
) {
    suspend operator fun invoke(id: String) {
        if (settings.current().wallpaperId == id) {
            settings.setWallpaperId(null)
        }
        wallpapers.deleteUser(id)
    }
}
