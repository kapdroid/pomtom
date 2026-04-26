package com.kapdroid.pomtom.domain.entity

enum class GoalType { SESSIONS, MINUTES, HOURS, DAYS }

enum class GoalAttachMode { NEXT_SESSION, ALL_SESSIONS, MANUAL }

enum class GoalColor { AMBER, EMBER, ROSE, SAGE, VIOLET }

data class Goal(
    val id: String,
    val title: String,
    val type: GoalType,
    val target: Int,
    val progress: Int,
    val attachMode: GoalAttachMode,
    val color: GoalColor,
    val createdAtMs: Long,
    val completedAtMs: Long?,
) {
    val isCompleted: Boolean get() = completedAtMs != null || progress >= target
    val percent: Float get() = if (target == 0) 0f else (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    fun applySessionContribution(focusedMs: Long): Goal {
        if (isCompleted) return this
        val delta = when (type) {
            GoalType.SESSIONS -> 1
            GoalType.MINUTES -> (focusedMs / 60_000L).toInt()
            GoalType.HOURS -> (focusedMs / 3_600_000L).toInt()
            GoalType.DAYS -> 1
        }
        if (delta <= 0) return this
        val next = (progress + delta).coerceAtMost(target)
        return copy(progress = next)
    }
}
