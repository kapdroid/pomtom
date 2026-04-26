package com.kapdroid.pomtom.audio

data class AudioMetadata(val durationMs: Long, val sizeBytes: Long)

expect class AudioProbe {
    suspend fun probe(filePath: String): Result<AudioMetadata>
}
