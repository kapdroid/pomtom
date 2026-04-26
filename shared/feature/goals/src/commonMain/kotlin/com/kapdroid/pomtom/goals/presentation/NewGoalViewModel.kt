package com.kapdroid.pomtom.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.usecase.CreateGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewGoalUiState(
    val title: String = "",
    val type: GoalType = GoalType.SESSIONS,
    val target: Int = 25,
    val color: GoalColor = GoalColor.AMBER,
    val attachOnSave: Boolean = true,
    val isSaving: Boolean = false,
    val savedId: String? = null,
    val errorMessage: String? = null,
) {
    val canSave: Boolean get() = title.isNotBlank() && target > 0 && !isSaving
}

sealed interface NewGoalUiEvent {
    data class TitleChanged(val value: String) : NewGoalUiEvent
    data class TargetDelta(val delta: Int) : NewGoalUiEvent
    data class TypeSelected(val type: GoalType) : NewGoalUiEvent
    data class ColorSelected(val color: GoalColor) : NewGoalUiEvent
    data object ToggleAttachOnSave : NewGoalUiEvent
    data object Save : NewGoalUiEvent
    data object DismissError : NewGoalUiEvent
}

class NewGoalViewModel(
    private val createGoal: CreateGoalUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NewGoalUiState())
    val uiState: StateFlow<NewGoalUiState> = _state.asStateFlow()

    fun onEvent(event: NewGoalUiEvent) {
        when (event) {
            is NewGoalUiEvent.TitleChanged -> _state.update { it.copy(title = event.value) }
            is NewGoalUiEvent.TargetDelta -> _state.update {
                it.copy(target = (it.target + event.delta).coerceAtLeast(1))
            }
            is NewGoalUiEvent.TypeSelected -> _state.update { it.copy(type = event.type) }
            is NewGoalUiEvent.ColorSelected -> _state.update { it.copy(color = event.color) }
            NewGoalUiEvent.ToggleAttachOnSave -> _state.update { it.copy(attachOnSave = !it.attachOnSave) }
            NewGoalUiEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
            NewGoalUiEvent.Save -> save()
        }
    }

    private fun save() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val attachMode = if (current.attachOnSave) GoalAttachMode.NEXT_SESSION else GoalAttachMode.MANUAL
            runCatching {
                createGoal(
                    title = current.title,
                    type = current.type,
                    target = current.target,
                    attachMode = attachMode,
                    color = current.color,
                )
            }.onSuccess { goal ->
                _state.update { it.copy(isSaving = false, savedId = goal.id) }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(isSaving = false, errorMessage = throwable.message ?: "Could not save")
                }
            }
        }
    }

    fun consumeSaved() {
        _state.update { it.copy(savedId = null) }
    }
}
