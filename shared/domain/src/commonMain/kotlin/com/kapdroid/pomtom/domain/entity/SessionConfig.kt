package com.kapdroid.pomtom.domain.entity

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class SessionConfig(
    val focus: Duration = 25.minutes,
    val shortBreak: Duration = 5.minutes,
    val longBreak: Duration = 20.minutes,
    val cyclesBeforeLong: Int = 4,
    val strictMode: Boolean = false,
) {
    init {
        require(focus.inWholeSeconds in 60..(180 * 60)) { "focus must be 1m..180m" }
        require(shortBreak.inWholeSeconds in 60..(60 * 60)) { "shortBreak must be 1m..60m" }
        require(longBreak.inWholeSeconds in 60..(120 * 60)) { "longBreak must be 1m..120m" }
        require(cyclesBeforeLong in 1..10) { "cyclesBeforeLong must be 1..10" }
    }

    fun phaseAt(cycleIndex: Int): SessionPhase = when {
        cycleIndex % 2 == 0 -> SessionPhase.FOCUS
        ((cycleIndex + 1) / 2) % cyclesBeforeLong == 0 -> SessionPhase.LONG_BREAK
        else -> SessionPhase.SHORT_BREAK
    }

    fun durationFor(phase: SessionPhase): Duration = when (phase) {
        SessionPhase.FOCUS -> focus
        SessionPhase.SHORT_BREAK -> shortBreak
        SessionPhase.LONG_BREAK -> longBreak
    }
}
