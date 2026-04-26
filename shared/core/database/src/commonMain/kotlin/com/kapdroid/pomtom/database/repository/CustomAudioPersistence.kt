package com.kapdroid.pomtom.database.repository

import app.cash.sqldelight.coroutines.asFlow
import com.kapdroid.pomtom.common.IdGenerator
import com.kapdroid.pomtom.database.PomtomDatabase
import com.kapdroid.pomtom.database.internal.toAudioTrack
import com.kapdroid.pomtom.domain.entity.AudioTrack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CustomAudioPersistence(
    private val database: PomtomDatabase,
    private val idGenerator: IdGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val q get() = database.customAudioTrackQueries

    fun observeAll(): Flow<List<AudioTrack>> =
        q.selectAll().asFlow().map { it.executeAsList().map { row -> row.toAudioTrack() } }.flowOn(dispatcher)

    suspend fun getById(id: String): AudioTrack? = withContext(dispatcher) {
        q.selectById(id).executeAsOneOrNull()?.toAudioTrack()
    }

    suspend fun insert(
        displayName: String,
        localPath: String,
        durationMs: Long,
        sizeBytes: Long,
        addedAtMs: Long,
    ): AudioTrack = withContext(dispatcher) {
        val id = idGenerator.newId()
        q.insert(
            id = id,
            display_name = displayName,
            local_path = localPath,
            duration_ms = durationMs,
            size_bytes = sizeBytes,
            added_at = addedAtMs,
        )
        getById(id)!!
    }

    suspend fun delete(id: String) = withContext(dispatcher) {
        q.deleteById(id)
    }
}
