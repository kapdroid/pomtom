package com.kapdroid.pomtom.stats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

data class HistoryEntry(
    val session: FocusSession,
    val goalTitle: String?,
)

data class HistoryDayGroup(
    val date: LocalDate,
    val entries: List<HistoryEntry>,
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val groups: List<HistoryDayGroup> = emptyList(),
)

class HistoryViewModel(
    sessionRepository: SessionRepository,
    goalsRepository: GoalsRepository,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        sessionRepository.observeRecent(limit = 200),
        goalsRepository.observeAll(),
    ) { sessions: List<FocusSession>, goals: List<Goal> ->
        val goalsById = goals.associateBy { it.id }
        val zone = clock.timeZone()
        val groups = sessions
            .filter { it.isFinished }
            .groupBy { Instant.fromEpochMilliseconds(it.startedAtMs).toLocalDateTime(zone).date }
            .entries
            .sortedByDescending { it.key }
            .map { (date, daySessions) ->
                HistoryDayGroup(
                    date = date,
                    entries = daySessions
                        .sortedByDescending { it.startedAtMs }
                        .map { HistoryEntry(it, it.goalId?.let { id -> goalsById[id]?.title }) },
                )
            }
        HistoryUiState(isLoading = false, groups = groups)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HistoryUiState(),
    )
}
