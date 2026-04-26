package com.kapdroid.pomtom.domain.entity

enum class SessionPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

enum class SessionStatus { RUNNING, PAUSED, COMPLETED, ABORTED }

data class FocusSession(
    val id: String,
    val goalId: String?,
    val startedAtMs: Long,
    val resumedAtMs: Long,
    val endedAtMs: Long?,
    val plannedMs: Long,
    val actualMs: Long,
    val phase: SessionPhase,
    val cycleIndex: Int,
    val status: SessionStatus,
    val strictMode: Boolean,
) {
    val isActive: Boolean get() = status == SessionStatus.RUNNING || status == SessionStatus.PAUSED
    val isFinished: Boolean get() = status == SessionStatus.COMPLETED || status == SessionStatus.ABORTED
}
