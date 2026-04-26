package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.domain.repository.SessionRepository

class PauseFocusSessionUseCase(
    private val sessions: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(sessionId: String, accumulatedMs: Long) {
        sessions.pause(sessionId, clock.nowMs(), accumulatedMs)
    }
}

class ResumeFocusSessionUseCase(
    private val sessions: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(sessionId: String) {
        sessions.resume(sessionId, clock.nowMs())
    }
}
