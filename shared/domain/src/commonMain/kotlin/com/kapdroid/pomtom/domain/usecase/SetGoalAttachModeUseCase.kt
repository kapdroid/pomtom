package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.repository.GoalsRepository

class SetGoalAttachModeUseCase(private val goals: GoalsRepository) {
    suspend operator fun invoke(goalId: String, mode: GoalAttachMode) {
        val current = goals.getById(goalId) ?: return
        if (current.attachMode == mode) return
        goals.update(current.copy(attachMode = mode))
    }
}
