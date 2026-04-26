package com.kapdroid.pomtom.audio

import android.media.MediaMetadataRetriever
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class AudioProbe {

    actual suspend fun probe(filePath: String): Result<AudioMetadata> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(filePath)
            require(file.exists()) { "File not found: $filePath" }
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(filePath)
                val durationMs = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?: error("Cannot read duration for $filePath")
                AudioMetadata(durationMs = durationMs, sizeBytes = file.length())
            } finally {
                runCatching { retriever.release() }
            }
        }.onFailure { Napier.w(it) { "Audio probe failed for $filePath" } }
    }
}
