package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository

class StartFocusSessionUseCase(
    private val sessions: SessionRepository,
    private val goals: GoalsRepository,
    private val settings: SettingsRepository,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        phase: SessionPhase = SessionPhase.FOCUS,
        cycleIndex: Int = 0,
        explicitGoalId: String? = null,
    ): FocusSession {
        val current = settings.current()
        val plannedMs = current.sessionConfig.durationFor(phase).inWholeMilliseconds
        val attachedGoalId = explicitGoalId ?: when (phase) {
            SessionPhase.FOCUS -> goals.nextAttachable()?.takeIf { it.attachMode != GoalAttachMode.MANUAL }?.id
            else -> null
        }
        val started = clock.nowMs()
        val session = sessions.start(
            plannedMs = plannedMs,
            phase = phase,
            cycleIndex = cycleIndex,
            strictMode = current.sessionConfig.strictMode,
            goalId = attachedGoalId,
            startedAtMs = started,
        )
        eventBus.emit(DomainEvent.SessionStarted(session.id, attachedGoalId))
        return session
    }
}
