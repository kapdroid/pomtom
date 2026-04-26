package com.kapdroid.pomtom.stats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.DailyFocus
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import com.kapdroid.pomtom.domain.repository.StatsRepository
import com.kapdroid.pomtom.domain.repository.StatsSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val isLoading: Boolean = true,
    val totalFocusedMs: Long = 0L,
    val sessionCount: Int = 0,
    val streakDays: Int = 0,
    val goalsCompleted: Int = 0,
    val goalCompletionRate: Float = 0f,
    val byDay: List<DailyFocus> = emptyList(),
    val recentSessions: List<RecentSessionItem> = emptyList(),
)

data class RecentSessionItem(
    val session: FocusSession,
    val goalTitle: String?,
)

class StatsViewModel(
    statsRepository: StatsRepository,
    sessionRepository: SessionRepository,
    goalsRepository: GoalsRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        statsRepository.observeSnapshot(rangeDays = 7),
        sessionRepository.observeRecent(limit = 5),
        goalsRepository.observeAll(),
    ) { snapshot: StatsSnapshot, recent: List<FocusSession>, goals: List<Goal> ->
        val goalsById = goals.associateBy { it.id }
        val items = recent
            .filter { it.phase == SessionPhase.FOCUS && (it.status == SessionStatus.COMPLETED || it.status == SessionStatus.ABORTED) }
            .map { session ->
                RecentSessionItem(
                    session = session,
                    goalTitle = session.goalId?.let { goalsById[it]?.title },
                )
            }
        StatsUiState(
            isLoading = false,
            totalFocusedMs = snapshot.totalFocusedMs,
            sessionCount = snapshot.sessionCount,
            streakDays = snapshot.streakDays,
            goalsCompleted = snapshot.goalsCompleted,
            goalCompletionRate = snapshot.goalCompletionRate,
            byDay = snapshot.byDay,
            recentSessions = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StatsUiState(),
    )
}
