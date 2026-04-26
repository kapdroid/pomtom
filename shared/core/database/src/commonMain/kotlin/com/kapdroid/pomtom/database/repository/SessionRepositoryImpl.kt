package com.kapdroid.pomtom.database.repository

import app.cash.sqldelight.coroutines.asFlow
import com.kapdroid.pomtom.common.IdGenerator
import com.kapdroid.pomtom.database.PomtomDatabase
import com.kapdroid.pomtom.database.internal.toDomain
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val database: PomtomDatabase,
    private val idGenerator: IdGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SessionRepository {

    private val q get() = database.focusSessionQueries

    override fun observeActive(): Flow<FocusSession?> =
        q.selectActive().asFlow().map { it.executeAsOneOrNull()?.toDomain() }.flowOn(dispatcher)

    override fun observeRecent(limit: Int): Flow<List<FocusSession>> =
        q.selectRecent(limit.toLong()).asFlow()
            .map { it.executeAsList().map { row -> row.toDomain() } }
            .flowOn(dispatcher)

    override fun observeBetween(fromMs: Long, toMs: Long): Flow<List<FocusSession>> =
        q.selectBetween(fromMs, toMs).asFlow()
            .map { it.executeAsList().map { row -> row.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun start(
        plannedMs: Long,
        phase: SessionPhase,
        cycleIndex: Int,
        strictMode: Boolean,
        goalId: String?,
        startedAtMs: Long,
    ): FocusSession = withContext(dispatcher) {
        val id = idGenerator.newId()
        q.insert(
            id = id,
            goal_id = goalId,
            started_at = startedAtMs,
            resumed_at = startedAtMs,
            ended_at = null,
            planned_ms = plannedMs,
            actual_ms = 0L,
            phase = phase.name,
            cycle_index = cycleIndex.toLong(),
            status = SessionStatus.RUNNING.name,
            strict_mode = if (strictMode) 1L else 0L,
        )
        FocusSession(
            id = id,
            goalId = goalId,
            startedAtMs = startedAtMs,
            resumedAtMs = startedAtMs,
            endedAtMs = null,
            plannedMs = plannedMs,
            actualMs = 0,
            phase = phase,
            cycleIndex = cycleIndex,
            status = SessionStatus.RUNNING,
            strictMode = strictMode,
        )
    }

    override suspend fun pause(sessionId: String, atMs: Long, accumulatedMs: Long) = withContext(dispatcher) {
        q.updatePaused(actual_ms = accumulatedMs, id = sessionId)
    }

    override suspend fun resume(sessionId: String, atMs: Long) = withContext(dispatcher) {
        q.updateResumed(resumed_at = atMs, id = sessionId)
    }

    override suspend fun complete(sessionId: String, atMs: Long, actualMs: Long): FocusSession = withContext(dispatcher) {
        q.updateStatus(
            status = SessionStatus.COMPLETED.name,
            ended_at = atMs,
            actual_ms = actualMs,
            id = sessionId,
        )
        q.selectById(sessionId).executeAsOne().toDomain()
    }

    override suspend fun abort(sessionId: String, atMs: Long, actualMs: Long): FocusSession = withContext(dispatcher) {
        q.updateStatus(
            status = SessionStatus.ABORTED.name,
            ended_at = atMs,
            actual_ms = actualMs,
            id = sessionId,
        )
        q.selectById(sessionId).executeAsOne().toDomain()
    }

    override suspend fun getById(sessionId: String): FocusSession? = withContext(dispatcher) {
        q.selectById(sessionId).executeAsOneOrNull()?.toDomain()
    }
}
