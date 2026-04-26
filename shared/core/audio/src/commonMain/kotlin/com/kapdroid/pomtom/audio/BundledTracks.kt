package com.kapdroid.pomtom.audio

import com.kapdroid.pomtom.domain.entity.AudioCategory
import com.kapdroid.pomtom.domain.entity.AudioSourceKind
import com.kapdroid.pomtom.domain.entity.AudioTrack

/**
 * Curated ambient tracks that ship with the app.
 * Resource paths point at files under `composeApp/src/commonMain/composeResources/files/audio/`.
 * Single-track playback model — only one of these can play at a time.
 */
object BundledTracks {
    val all: List<AudioTrack> = listOf(
        bundled("lofi", "Lo-fi Drift", AudioCategory.AMBIENT),
        bundled("rain", "Soft Rain", AudioCategory.NATURE),
        bundled("storm", "Distant Storm", AudioCategory.NATURE),
        bundled("forest", "Forest Floor", AudioCategory.NATURE),
        bundled("ocean", "Slow Ocean", AudioCategory.NATURE),
        bundled("cafe", "Café Hum", AudioCategory.AMBIENT),
        bundled("fire", "Crackling Fire", AudioCategory.NATURE),
        bundled("piano", "Felt Piano", AudioCategory.INSTRUMENT),
        bundled("vinyl", "Vinyl Hush", AudioCategory.AMBIENT),
    )

    fun resourcePathFor(track: AudioTrack): String? = track.resourcePath

    private fun bundled(id: String, displayName: String, category: AudioCategory): AudioTrack = AudioTrack(
        id = "bundled_$id",
        displayName = displayName,
        category = category,
        source = AudioSourceKind.BUNDLED,
        resourcePath = "files/audio/$id.opus",
        localPath = null,
        durationMs = 0L,
        sizeBytes = 0L,
        addedAtMs = 0L,
    )
}
