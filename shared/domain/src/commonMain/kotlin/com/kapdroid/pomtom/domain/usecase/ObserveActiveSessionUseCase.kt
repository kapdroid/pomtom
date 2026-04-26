package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveSessionUseCase(
    private val sessions: SessionRepository,
) {
    operator fun invoke(): Flow<FocusSession?> = sessions.observeActive()
}
