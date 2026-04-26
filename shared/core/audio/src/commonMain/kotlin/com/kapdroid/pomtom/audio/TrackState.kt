package com.kapdroid.pomtom.audio

data class TrackState(
    val trackId: String,
    val isLoaded: Boolean = false,
    val isPlaying: Boolean = false,
    val volume: Float = 0.6f,
    val errorMessage: String? = null,
)
