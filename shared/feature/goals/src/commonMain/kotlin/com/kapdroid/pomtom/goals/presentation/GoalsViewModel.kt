package com.kapdroid.pomtom.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.usecase.DeleteGoalUseCase
import com.kapdroid.pomtom.domain.usecase.SetGoalAttachModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GoalsFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DONE("Done"),
}

data class GoalsUiState(
    val isLoading: Boolean = true,
    val all: List<Goal> = emptyList(),
    val visible: List<Goal> = emptyList(),
    val filter: GoalsFilter = GoalsFilter.ALL,
    val activeCount: Int = 0,
    val totalSessions: Int = 0,
)

sealed interface GoalsUiEvent {
    data class SelectFilter(val filter: GoalsFilter) : GoalsUiEvent
    data class ToggleAttach(val goalId: String) : GoalsUiEvent
    data class Delete(val goalId: String) : GoalsUiEvent
}

class GoalsViewModel(
    private val goalsRepository: GoalsRepository,
    private val setAttachMode: SetGoalAttachModeUseCase,
    private val deleteGoal: DeleteGoalUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(GoalsFilter.ALL)

    val uiState: StateFlow<GoalsUiState> =
        combine(goalsRepository.observeAll(), filter) { goals, currentFilter ->
            val visible = when (currentFilter) {
                GoalsFilter.ALL -> goals
                GoalsFilter.ACTIVE -> goals.filter { !it.isCompleted }
                GoalsFilter.DONE -> goals.filter { it.isCompleted }
            }
            GoalsUiState(
                isLoading = false,
                all = goals,
                visible = visible,
                filter = currentFilter,
                activeCount = goals.count { !it.isCompleted },
                totalSessions = goals.sumOf { it.progress },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = GoalsUiState(),
        )

    fun onEvent(event: GoalsUiEvent) {
        when (event) {
            is GoalsUiEvent.SelectFilter -> filter.value = event.filter
            is GoalsUiEvent.ToggleAttach -> viewModelScope.launch {
                val goal = goalsRepository.getById(event.goalId) ?: return@launch
                val nextMode = if (goal.attachMode == GoalAttachMode.NEXT_SESSION) {
                    GoalAttachMode.MANUAL
                } else {
                    GoalAttachMode.NEXT_SESSION
                }
                setAttachMode(goal.id, nextMode)
            }
            is GoalsUiEvent.Delete -> viewModelScope.launch { deleteGoal(event.goalId) }
        }
    }
}
