package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.AppError
import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.common.Result
import com.kapdroid.pomtom.common.runCatchingDomain
import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.repository.WallpaperRepository

class ImportWallpaperUseCase(
    private val wallpapers: WallpaperRepository,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(sourceUri: String, displayName: String): Result<Wallpaper> {
        if (sourceUri.isBlank()) return Result.Failure(AppError.Validation("source uri required"))
        val title = displayName.trim().ifBlank { "Wallpaper" }
        return runCatchingDomain {
            wallpapers.importUser(sourceUri, title, clock.nowMs())
        }.also { result ->
            result.getOrNull()?.let { eventBus.emit(DomainEvent.WallpaperImported(it.id)) }
        }
    }
}
