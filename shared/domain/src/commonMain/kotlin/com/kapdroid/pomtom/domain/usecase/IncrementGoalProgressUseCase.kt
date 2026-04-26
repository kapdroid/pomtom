package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.repository.GoalsRepository

class IncrementGoalProgressUseCase(
    private val goals: GoalsRepository,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(goalId: String, focusedMs: Long): Goal? {
        val current = goals.getById(goalId) ?: return null
        if (current.isCompleted) return current

        val next = current.applySessionContribution(focusedMs)
        if (next.progress == current.progress) return current
        goals.update(next)
        eventBus.emit(DomainEvent.GoalProgressed(goalId, next.progress, next.target))

        if (next.progress >= next.target) {
            val now = clock.nowMs()
            goals.markCompleted(goalId, now)
            eventBus.emit(DomainEvent.GoalCompleted(goalId))
            return next.copy(completedAtMs = now)
        }
        return next
    }
}
