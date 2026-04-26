package com.kapdroid.pomtom.timer.presentation

import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Emits monotonic ticks driven by wall-clock time so that backgrounding
 * doesn't desynchronise the ring. The ViewModel computes elapsed/remaining
 * by subtracting `started_at` (and accumulated `actual_ms`) from the clock,
 * not by counting coroutine ticks.
 */
class SessionTickerService(
    private val clock: Clock,
    private val tickIntervalMs: Long = 250L,
) {
    fun ticks(): Flow<Long> = flow {
        while (true) {
            emit(clock.nowMs())
            delay(tickIntervalMs)
        }
    }

    fun computeElapsedMs(session: FocusSession, nowMs: Long): Long {
        val frozen = session.actualMs
        return when (session.status) {
            SessionStatus.RUNNING -> frozen + (nowMs - session.resumedAtMs).coerceAtLeast(0L)
            SessionStatus.PAUSED -> frozen
            SessionStatus.COMPLETED, SessionStatus.ABORTED -> frozen
        }
    }

    fun computeRemainingMs(session: FocusSession, nowMs: Long): Long {
        val elapsed = computeElapsedMs(session, nowMs)
        return (session.plannedMs - elapsed).coerceAtLeast(0L)
    }

    fun computeProgress(session: FocusSession, nowMs: Long): Float {
        if (session.plannedMs <= 0L) return 0f
        val elapsed = computeElapsedMs(session, nowMs)
        return (elapsed.toFloat() / session.plannedMs.toFloat()).coerceIn(0f, 1f)
    }
}
