package com.kapdroid.pomtom.domain.repository

import com.kapdroid.pomtom.domain.entity.AudioTrack
import kotlinx.coroutines.flow.Flow

interface AudioLibraryRepository {
    fun observeBundled(): Flow<List<AudioTrack>>
    fun observeCustom(): Flow<List<AudioTrack>>
    fun observeAll(): Flow<List<AudioTrack>>

    suspend fun getById(trackId: String): AudioTrack?
    suspend fun importCustom(sourceUri: String, displayName: String, addedAtMs: Long): AudioTrack
    suspend fun deleteCustom(trackId: String)
}
