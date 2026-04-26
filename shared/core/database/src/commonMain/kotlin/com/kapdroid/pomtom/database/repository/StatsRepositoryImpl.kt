package com.kapdroid.pomtom.database.repository

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.database.PomtomDatabase
import com.kapdroid.pomtom.database.internal.toDomain
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.DailyFocus
import com.kapdroid.pomtom.domain.repository.StatsRepository
import com.kapdroid.pomtom.domain.repository.StatsSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import app.cash.sqldelight.coroutines.asFlow

class StatsRepositoryImpl(
    private val database: PomtomDatabase,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : StatsRepository {

    private val sessionQ get() = database.focusSessionQueries
    private val goalQ get() = database.goalQueries

    override fun observeSnapshot(rangeDays: Int): Flow<StatsSnapshot> =
        sessionQ.selectRecent(limit = 500L).asFlow().map { _ ->
            val today = clock.today()
            val from = today.minus(DatePeriod(days = rangeDays - 1)).atStartOfDayIn(clock.timeZone()).toEpochMilliseconds()
            val to = (today.plus(DatePeriod(days = 1))).atStartOfDayIn(clock.timeZone()).toEpochMilliseconds() - 1
            val sessions = sessionQ.selectBetween(from, to).executeAsList().map { it.toDomain() }
            val focusOnly = sessions.filter { it.phase == SessionPhase.FOCUS && it.status == SessionStatus.COMPLETED }
            val totalFocusedMs = focusOnly.sumOf { it.actualMs }
            val byDay = bucketByDay(focusOnly, from, today, rangeDays)
            val streakDays = computeStreak(byDay, today)
            val completed = goalQ.countCompleted().executeAsOne()
            val totalGoals = goalQ.countTotal().executeAsOne()
            val rate = if (totalGoals == 0L) 0f else completed.toFloat() / totalGoals.toFloat()
            StatsSnapshot(
                totalFocusedMs = totalFocusedMs,
                sessionCount = focusOnly.size,
                streakDays = streakDays,
                goalsCompleted = completed.toInt(),
                goalCompletionRate = rate,
                byDay = byDay,
            )
        }.flowOn(dispatcher)

    override fun observeDaily(rangeDays: Int): Flow<List<DailyFocus>> =
        sessionQ.selectRecent(limit = 1000L).asFlow().map { _ ->
            val today = clock.today()
            val from = today.minus(DatePeriod(days = rangeDays - 1)).atStartOfDayIn(clock.timeZone()).toEpochMilliseconds()
            val to = (today.plus(DatePeriod(days = 1))).atStartOfDayIn(clock.timeZone()).toEpochMilliseconds() - 1
            val sessions = sessionQ.selectBetween(from, to).executeAsList().map { it.toDomain() }
                .filter { it.phase == SessionPhase.FOCUS && it.status == SessionStatus.COMPLETED }
            bucketByDay(sessions, from, today, rangeDays)
        }.flowOn(dispatcher)

    private fun bucketByDay(
        sessions: List<com.kapdroid.pomtom.domain.entity.FocusSession>,
        from: Long,
        today: LocalDate,
        rangeDays: Int,
    ): List<DailyFocus> {
        val zone = clock.timeZone()
        val grouped = sessions.groupBy { Instant.fromEpochMilliseconds(it.startedAtMs).toLocalDateTime(zone).date }
        return (0 until rangeDays).map { offset ->
            val date = today.minus(DatePeriod(days = rangeDays - 1 - offset))
            val day = grouped[date].orEmpty()
            DailyFocus(date, day.sumOf { it.actualMs }, day.size)
        }
    }

    private fun computeStreak(byDay: List<DailyFocus>, today: LocalDate): Int {
        var streak = 0
        for (i in byDay.indices.reversed()) {
            val entry = byDay[i]
            if (entry.focusedMs > 0) streak++ else break
            if (entry.date == today && entry.focusedMs == 0L) break
        }
        return streak
    }
}
