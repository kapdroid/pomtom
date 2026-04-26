package com.kapdroid.pomtom.domain.fakes

import com.kapdroid.pomtom.domain.entity.AppSettings
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.entity.MotionIntensity
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSettingsRepository(initial: AppSettings = AppSettings.Defaults) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override fun observe(): Flow<AppSettings> = state
    override suspend fun current(): AppSettings = state.value
    override suspend fun setSessionConfig(config: SessionConfig) {
        state.value = state.value.copy(sessionConfig = config)
    }
    override suspend fun setStrictMode(enabled: Boolean) {
        state.value = state.value.copy(sessionConfig = state.value.sessionConfig.copy(strictMode = enabled))
    }
    override suspend fun setTheme(theme: AppTheme) {
        state.value = state.value.copy(theme = theme)
    }
    override suspend fun setWallpaperId(id: String?) {
        state.value = state.value.copy(wallpaperId = id)
    }
    override suspend fun setMotion(motion: MotionIntensity) {
        state.value = state.value.copy(motion = motion)
    }
    override suspend fun setMasterVolume(volume: Float) {
        state.value = state.value.copy(masterVolume = volume.coerceIn(0f, 1f))
    }
    override suspend fun setFirstRunComplete(value: Boolean) {
        state.value = state.value.copy(firstRunComplete = value)
    }
    override suspend fun setFocusAudioTrackId(id: String?) {
        state.value = state.value.copy(focusAudioTrackId = id)
    }
}

class FakeGoalsRepository : GoalsRepository {
    private val state = MutableStateFlow<List<Goal>>(emptyList())
    private var idSeq = 0

    override fun observeAll(): Flow<List<Goal>> = state
    override fun observeActive(): Flow<List<Goal>> = state.map { list -> list.filter { it.completedAtMs == null } }
    override fun observeById(goalId: String): Flow<Goal?> = state.map { list -> list.firstOrNull { it.id == goalId } }
    override suspend fun getById(goalId: String): Goal? = state.value.firstOrNull { it.id == goalId }
    override suspend fun create(
        title: String,
        type: GoalType,
        target: Int,
        attachMode: GoalAttachMode,
        color: GoalColor,
        createdAtMs: Long,
    ): Goal {
        val goal = Goal(
            id = "g${++idSeq}",
            title = title,
            type = type,
            target = target,
            progress = 0,
            attachMode = attachMode,
            color = color,
            createdAtMs = createdAtMs,
            completedAtMs = null,
        )
        state.value = state.value + goal
        return goal
    }
    override suspend fun update(goal: Goal) {
        state.value = state.value.map { if (it.id == goal.id) goal else it }
    }
    override suspend fun delete(goalId: String) {
        state.value = state.value.filterNot { it.id == goalId }
    }
    override suspend fun markCompleted(goalId: String, atMs: Long) {
        state.value = state.value.map { if (it.id == goalId) it.copy(completedAtMs = atMs, progress = it.target) else it }
    }
    override suspend fun nextAttachable(): Goal? = state.value.firstOrNull {
        it.completedAtMs == null && it.attachMode == GoalAttachMode.NEXT_SESSION
    }
    override suspend fun allAttachableForSession(): List<Goal> = state.value.filter {
        it.completedAtMs == null && it.attachMode != GoalAttachMode.MANUAL
    }
}

class FakeSessionRepository : SessionRepository {
    val items = MutableStateFlow<List<FocusSession>>(emptyList())
    private var idSeq = 0

    override fun observeActive(): Flow<FocusSession?> = items.map { list -> list.firstOrNull { it.isActive } }
    override fun observeRecent(limit: Int): Flow<List<FocusSession>> = items.map { it.takeLast(limit).reversed() }
    override fun observeBetween(fromMs: Long, toMs: Long): Flow<List<FocusSession>> =
        items.map { list -> list.filter { it.startedAtMs in fromMs..toMs } }

    override suspend fun start(
        plannedMs: Long,
        phase: SessionPhase,
        cycleIndex: Int,
        strictMode: Boolean,
        goalId: String?,
        startedAtMs: Long,
    ): FocusSession {
        val s = FocusSession(
            id = "s${++idSeq}",
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
        items.value = items.value + s
        return s
    }
    override suspend fun pause(sessionId: String, atMs: Long, accumulatedMs: Long) = mutate(sessionId) {
        it.copy(status = SessionStatus.PAUSED, actualMs = accumulatedMs)
    }
    override suspend fun resume(sessionId: String, atMs: Long) = mutate(sessionId) {
        it.copy(status = SessionStatus.RUNNING, resumedAtMs = atMs)
    }
    override suspend fun complete(sessionId: String, atMs: Long, actualMs: Long): FocusSession {
        var result: FocusSession? = null
        items.value = items.value.map {
            if (it.id == sessionId) {
                val updated = it.copy(status = SessionStatus.COMPLETED, endedAtMs = atMs, actualMs = actualMs)
                result = updated
                updated
            } else it
        }
        return result!!
    }
    override suspend fun abort(sessionId: String, atMs: Long, actualMs: Long): FocusSession {
        var result: FocusSession? = null
        items.value = items.value.map {
            if (it.id == sessionId) {
                val updated = it.copy(status = SessionStatus.ABORTED, endedAtMs = atMs, actualMs = actualMs)
                result = updated
                updated
            } else it
        }
        return result!!
    }
    override suspend fun getById(sessionId: String): FocusSession? = items.value.firstOrNull { it.id == sessionId }

    private fun mutate(sessionId: String, transform: (FocusSession) -> FocusSession) {
        items.value = items.value.map { if (it.id == sessionId) transform(it) else it }
    }
}
