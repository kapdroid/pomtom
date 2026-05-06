package com.kapdroid.pomtom.audio

import kotlinx.coroutines.flow.StateFlow

expect class AudioEngine {
    val state: StateFlow<Map<String, TrackState>>

    suspend fun loadTrack(trackId: String, source: AudioSource): Result<Unit>
    fun setVolume(trackId: String, gain: Float)
    fun play(trackId: String)
    fun pause(trackId: String)
    fun stop(trackId: String)
    fun playAll()
    fun pauseAll()
    fun stopAll()
    fun unload(trackId: String)

    /**
     * Play a short bundled sound once and release the underlying player when it
     * finishes. Bypasses the loaded-track map — fire-and-forget. Used for the
     * session-completion chime; reusable for any short SFX (button press, alert).
     *
     * - Does not loop.
     * - Does not affect [state] (one-shots aren't tracked there).
     * - On Android, requests transient audio focus so any playing ambient ducks.
     * - [gain] is linear in [0f, 1f]; passing 0f is a no-op (no playback occurs).
     */
    fun playOneShot(source: AudioSource, gain: Float = 1f)

    fun release()
}
