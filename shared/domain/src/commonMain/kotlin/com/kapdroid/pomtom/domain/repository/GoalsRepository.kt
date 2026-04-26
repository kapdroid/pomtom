package com.kapdroid.pomtom.domain.repository

import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    fun observeAll(): Flow<List<Goal>>
    fun observeActive(): Flow<List<Goal>>
    fun observeById(goalId: String): Flow<Goal?>

    suspend fun getById(goalId: String): Goal?
    suspend fun create(
        title: String,
        type: GoalType,
        target: Int,
        attachMode: GoalAttachMode,
        color: GoalColor,
        createdAtMs: Long,
    ): Goal

    suspend fun update(goal: Goal)
    suspend fun delete(goalId: String)
    suspend fun markCompleted(goalId: String, atMs: Long)

    suspend fun nextAttachable(): Goal?
    suspend fun allAttachableForSession(): List<Goal>
}
