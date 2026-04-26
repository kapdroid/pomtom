package com.kapdroid.pomtom.domain.entity

enum class AudioCategory { NATURE, AMBIENT, INSTRUMENT }

enum class AudioSourceKind { BUNDLED, CUSTOM }

data class AudioTrack(
    val id: String,
    val displayName: String,
    val category: AudioCategory,
    val source: AudioSourceKind,
    val resourcePath: String?,
    val localPath: String?,
    val durationMs: Long,
    val sizeBytes: Long,
    val addedAtMs: Long,
) {
    init {
        require(source != AudioSourceKind.BUNDLED || resourcePath != null) {
            "bundled track requires resourcePath"
        }
        require(source != AudioSourceKind.CUSTOM || localPath != null) {
            "custom track requires localPath"
        }
    }
}
