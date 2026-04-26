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
    /**
     * Ambient creature shown during FOCUS phases on the session and strict screens.
     * Defaults to a calm meditating fox; users can switch creatures or disable
     * entirely from Settings → Focus companion.
     */
    val companion: CompanionType = CompanionType.Default,
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
                // Strict mode on out of the box — see SessionConfig.strictMode for the
                // rationale; AppSettings.Defaults is the single source of truth fed to
                // a fresh install before DataStore has anything written, so it must
                // agree with the data-class default.
                strictMode = true,
            ),
        )
    }
}
