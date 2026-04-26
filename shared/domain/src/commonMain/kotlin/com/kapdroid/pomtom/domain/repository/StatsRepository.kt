package com.kapdroid.pomtom.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

data class DailyFocus(val date: LocalDate, val focusedMs: Long, val sessionCount: Int)

data class StatsSnapshot(
    val totalFocusedMs: Long,
    val sessionCount: Int,
    val streakDays: Int,
    val goalsCompleted: Int,
    val goalCompletionRate: Float,
    val byDay: List<DailyFocus>,
)

interface StatsRepository {
    fun observeSnapshot(rangeDays: Int = 7): Flow<StatsSnapshot>
    fun observeDaily(rangeDays: Int = 30): Flow<List<DailyFocus>>
}
