package com.kapdroid.pomtom.common

sealed interface DomainEvent {
    data class SessionStarted(val sessionId: String, val goalId: String?) : DomainEvent
    data class SessionCompleted(val sessionId: String, val goalId: String?, val focusedMs: Long) : DomainEvent
    data class SessionAborted(val sessionId: String, val goalId: String?) : DomainEvent
    data class GoalCreated(val goalId: String) : DomainEvent
    data class GoalProgressed(val goalId: String, val progress: Int, val target: Int) : DomainEvent
    data class GoalCompleted(val goalId: String) : DomainEvent
    data class CustomAudioImported(val trackId: String) : DomainEvent
    data class WallpaperImported(val wallpaperId: String) : DomainEvent
    data object ThemeChanged : DomainEvent
}
