package com.kapdroid.pomtom.audio.data

import com.kapdroid.pomtom.audio.AudioProbe
import com.kapdroid.pomtom.audio.BundledTracks
import com.kapdroid.pomtom.database.repository.CustomAudioPersistence
import com.kapdroid.pomtom.domain.entity.AudioTrack
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AudioLibraryRepositoryImpl(
    private val customStore: CustomAudioPersistence,
    private val audioProbe: AudioProbe,
) : AudioLibraryRepository {

    private val bundled = MutableStateFlow(BundledTracks.all)

    override fun observeBundled(): Flow<List<AudioTrack>> = bundled

    override fun observeCustom(): Flow<List<AudioTrack>> = customStore.observeAll()

    override fun observeAll(): Flow<List<AudioTrack>> =
        observeBundled().combine(observeCustom()) { built, custom -> built + custom }
            .map { it.distinctBy(AudioTrack::id) }

    override suspend fun getById(trackId: String): AudioTrack? =
        bundled.value.firstOrNull { it.id == trackId }
            ?: customStore.getById(trackId)

    override suspend fun importCustom(
        sourceUri: String,
        displayName: String,
        addedAtMs: Long,
    ): AudioTrack {
        val metadata = audioProbe.probe(sourceUri).getOrElse {
            error("Failed to probe imported audio: ${it.message ?: "unknown"}")
        }
        return customStore.insert(
            displayName = displayName,
            localPath = sourceUri,
            durationMs = metadata.durationMs,
            sizeBytes = metadata.sizeBytes,
            addedAtMs = addedAtMs,
        )
    }

    override suspend fun deleteCustom(trackId: String) {
        customStore.delete(trackId)
    }
}
