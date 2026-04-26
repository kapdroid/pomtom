package com.kapdroid.pomtom.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CelebrateUiState(
    val isLoading: Boolean = true,
    val sessionId: String = "",
    val goal: Goal? = null,
    val sessionsCount: Int = 0,
    val focusedMs: Long = 0L,
    val brokenCount: Int = 0,
    val sessionFocusedMs: Long = 0L,
)

sealed interface CelebrateUiEvent {
    data class Bind(val sessionId: String, val goalId: String?) : CelebrateUiEvent
}

class CelebrateViewModel(
    private val goalsRepository: GoalsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CelebrateUiState())
    val uiState: StateFlow<CelebrateUiState> = _state.asStateFlow()

    fun onEvent(event: CelebrateUiEvent) {
        when (event) {
            is CelebrateUiEvent.Bind -> bind(event.sessionId, event.goalId)
        }
    }

    private fun bind(sessionId: String, goalId: String?) {
        if (_state.value.sessionId == sessionId && !_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, sessionId = sessionId) }
        viewModelScope.launch {
            val session = sessionRepository.getById(sessionId)
            val goal = goalId?.let { goalsRepository.getById(it) }
            val (sessions, focused, broken) = if (goalId != null) {
                aggregateForGoal(goalId)
            } else {
                Triple(0, 0L, 0)
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    goal = goal,
                    sessionsCount = sessions,
                    focusedMs = focused,
                    brokenCount = broken,
                    sessionFocusedMs = session?.actualMs ?: 0L,
                )
            }
        }
    }

    private suspend fun aggregateForGoal(goalId: String): Triple<Int, Long, Int> {
        val sessions = sessionRepository.observeRecent(limit = 500).first()
        var completed = 0
        var focusedMs = 0L
        var broken = 0
        sessions.forEach { s ->
            if (s.goalId != goalId) return@forEach
            if (s.phase != SessionPhase.FOCUS) return@forEach
            when (s.status) {
                SessionStatus.COMPLETED -> {
                    completed += 1
                    focusedMs += s.actualMs
                }
                SessionStatus.ABORTED -> broken += 1
                else -> Unit
            }
        }
        return Triple(completed, focusedMs, broken)
    }
}
