package com.kapdroid.pomtom.database.repository

import app.cash.sqldelight.coroutines.asFlow
import com.kapdroid.pomtom.common.IdGenerator
import com.kapdroid.pomtom.database.PomtomDatabase
import com.kapdroid.pomtom.database.internal.toDomain
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GoalsRepositoryImpl(
    private val database: PomtomDatabase,
    private val idGenerator: IdGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : GoalsRepository {

    private val q get() = database.goalQueries

    override fun observeAll(): Flow<List<Goal>> =
        q.selectAll().asFlow().map { it.executeAsList().map { row -> row.toDomain() } }.flowOn(dispatcher)

    override fun observeActive(): Flow<List<Goal>> =
        q.selectActive().asFlow().map { it.executeAsList().map { row -> row.toDomain() } }.flowOn(dispatcher)

    override fun observeById(goalId: String): Flow<Goal?> =
        q.selectById(goalId).asFlow().map { it.executeAsOneOrNull()?.toDomain() }.flowOn(dispatcher)

    override suspend fun getById(goalId: String): Goal? = withContext(dispatcher) {
        q.selectById(goalId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun create(
        title: String,
        type: GoalType,
        target: Int,
        attachMode: GoalAttachMode,
        color: GoalColor,
        createdAtMs: Long,
    ): Goal = withContext(dispatcher) {
        val id = idGenerator.newId()
        q.insert(
            id = id,
            title = title,
            type = type.name,
            target = target.toLong(),
            progress = 0L,
            attach_mode = attachMode.name,
            color = color.name,
            created_at = createdAtMs,
            completed_at = null,
        )
        Goal(
            id = id,
            title = title,
            type = type,
            target = target,
            progress = 0,
            attachMode = attachMode,
            color = color,
            createdAtMs = createdAtMs,
            completedAtMs = null,
        )
    }

    override suspend fun update(goal: Goal) = withContext(dispatcher) {
        q.update(
            title = goal.title,
            type = goal.type.name,
            target = goal.target.toLong(),
            progress = goal.progress.toLong(),
            attach_mode = goal.attachMode.name,
            color = goal.color.name,
            completed_at = goal.completedAtMs,
            id = goal.id,
        )
    }

    override suspend fun delete(goalId: String) = withContext(dispatcher) {
        q.deleteById(goalId)
    }

    override suspend fun markCompleted(goalId: String, atMs: Long) = withContext(dispatcher) {
        q.markCompleted(completed_at = atMs, id = goalId)
    }

    override suspend fun nextAttachable(): Goal? = withContext(dispatcher) {
        q.selectNextAttachable().executeAsOneOrNull()?.toDomain()
    }

    override suspend fun allAttachableForSession(): List<Goal> = withContext(dispatcher) {
        q.selectAllSessionAttachable().executeAsList().map { it.toDomain() }
    }
}
