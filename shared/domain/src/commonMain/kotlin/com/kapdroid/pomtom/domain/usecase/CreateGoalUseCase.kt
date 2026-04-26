package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.repository.GoalsRepository

class CreateGoalUseCase(
    private val goals: GoalsRepository,
    private val eventBus: EventBus,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        title: String,
        type: GoalType,
        target: Int,
        attachMode: GoalAttachMode = GoalAttachMode.NEXT_SESSION,
        color: GoalColor = GoalColor.AMBER,
    ): Goal {
        require(title.isNotBlank()) { "title is required" }
        require(target > 0) { "target must be > 0" }
        val goal = goals.create(title.trim(), type, target, attachMode, color, clock.nowMs())
        eventBus.emit(DomainEvent.GoalCreated(goal.id))
        return goal
    }
}

class DeleteGoalUseCase(private val goals: GoalsRepository) {
    suspend operator fun invoke(goalId: String) = goals.delete(goalId)
}
