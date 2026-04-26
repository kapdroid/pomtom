package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.AppError
import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.common.Result
import com.kapdroid.pomtom.common.runCatchingDomain
import com.kapdroid.pomtom.domain.entity.AudioTrack
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository

class ImportCustomAudioUseCase(
    private val library: AudioLibraryRepository,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(sourceUri: String, displayName: String): Result<AudioTrack> {
        if (sourceUri.isBlank()) return Result.Failure(AppError.Validation("source uri required"))
        val title = displayName.trim().ifBlank { "Custom track" }
        return runCatchingDomain {
            library.importCustom(sourceUri, title, clock.nowMs())
        }.also { result ->
            result.getOrNull()?.let { eventBus.emit(DomainEvent.CustomAudioImported(it.id)) }
        }
    }
}
