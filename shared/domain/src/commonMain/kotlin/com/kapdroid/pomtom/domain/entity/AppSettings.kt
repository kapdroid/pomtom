package com.kapdroid.pomtom.domain.entity

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class AppSettings(
    val sessionConfig: SessionConfig = SessionConfig(),
    val theme: AppTheme = AppTheme.Default,
    val wallpaperId: String? = null,
    val motion: MotionIntensity = MotionIntensity.STANDARD,
    val firstRunComplete: Boolean = false,
    val masterVolume: Float = 1f,
    /**
     * Identifier of the [com.kapdroid.pomtom.domain.entity.AudioTrack] that should auto-play
     * during FOCUS phases. `null` means the user has not chosen one (silent focus).
     */
    val focusAudioTrackId: String? = null,
) {
    val focus: Duration get() = sessionConfig.focus
    val shortBreak: Duration get() = sessionConfig.shortBreak
    val longBreak: Duration get() = sessionConfig.longBreak

    companion object {
        val Defaults = AppSettings(
            sessionConfig = SessionConfig(
                focus = 25.minutes,
                shortBreak = 5.minutes,
                longBreak = 20.minutes,
                cyclesBeforeLong = 4,
                strictMode = false,
            ),
        )
    }
}
