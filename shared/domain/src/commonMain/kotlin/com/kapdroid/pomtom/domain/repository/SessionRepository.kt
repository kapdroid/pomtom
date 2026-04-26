package com.kapdroid.pomtom.domain.repository

import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.SessionPhase
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeActive(): Flow<FocusSession?>
    fun observeRecent(limit: Int = 50): Flow<List<FocusSession>>
    fun observeBetween(fromMs: Long, toMs: Long): Flow<List<FocusSession>>

    suspend fun start(
        plannedMs: Long,
        phase: SessionPhase,
        cycleIndex: Int,
        strictMode: Boolean,
        goalId: String?,
        startedAtMs: Long,
    ): FocusSession

    suspend fun pause(sessionId: String, atMs: Long, accumulatedMs: Long)
    suspend fun resume(sessionId: String, atMs: Long)
    suspend fun complete(sessionId: String, atMs: Long, actualMs: Long): FocusSession
    suspend fun abort(sessionId: String, atMs: Long, actualMs: Long): FocusSession
    suspend fun getById(sessionId: String): FocusSession?
}
