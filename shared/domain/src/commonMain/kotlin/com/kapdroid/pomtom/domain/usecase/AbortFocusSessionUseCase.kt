package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.repository.SessionRepository

class AbortFocusSessionUseCase(
    private val sessions: SessionRepository,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(sessionId: String, actualMs: Long): FocusSession {
        val session = sessions.abort(sessionId, clock.nowMs(), actualMs)
        eventBus.emit(DomainEvent.SessionAborted(session.id, session.goalId))
        return session
    }
}
