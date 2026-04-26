package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.repository.SessionRepository

class CompleteFocusSessionUseCase(
    private val sessions: SessionRepository,
    private val incrementGoalProgress: IncrementGoalProgressUseCase,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(sessionId: String, actualMs: Long): FocusSession {
        val now = clock.nowMs()
        val session = sessions.complete(sessionId, now, actualMs)
        if (session.phase == SessionPhase.FOCUS) {
            session.goalId?.let { goalId ->
                incrementGoalProgress(goalId, session.actualMs)
            }
            eventBus.emit(DomainEvent.SessionCompleted(session.id, session.goalId, session.actualMs))
        }
        return session
    }
}
