package com.kapdroid.pomtom.audio

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.duration
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual class AudioProbe {

    actual suspend fun probe(filePath: String): Result<AudioMetadata> = runCatching {
        require(NSFileManager.defaultManager.fileExistsAtPath(filePath)) { "File not found: $filePath" }
        val url = NSURL.fileURLWithPath(filePath)
        val asset = AVURLAsset.URLAssetWithURL(url, options = null)
        val seconds = CMTimeGetSeconds(asset.duration)
        require(seconds.isFinite() && seconds >= 0.0) { "Invalid duration for $filePath" }
        val durationMs = (seconds * 1000.0).toLong()
        val sizeBytes = NSFileManager.defaultManager
            .attributesOfItemAtPath(filePath, error = null)
            ?.get(NSFileSize)
            ?.let { (it as? NSNumber)?.longLongValue }
            ?: 0L
        AudioMetadata(durationMs = durationMs, sizeBytes = sizeBytes)
    }.onFailure { Napier.w(it) { "Audio probe failed for $filePath" } }
}
