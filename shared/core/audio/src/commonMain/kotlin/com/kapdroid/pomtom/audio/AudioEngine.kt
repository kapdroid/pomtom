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
    fun release()
}
