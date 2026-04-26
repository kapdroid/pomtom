package com.kapdroid.pomtom.audio

sealed interface AudioSource {
    val key: String

    data class Bundled(val resourcePath: String) : AudioSource {
        override val key: String get() = "bundled:$resourcePath"
    }

    data class Local(val filePath: String) : AudioSource {
        override val key: String get() = "local:$filePath"
    }
}
