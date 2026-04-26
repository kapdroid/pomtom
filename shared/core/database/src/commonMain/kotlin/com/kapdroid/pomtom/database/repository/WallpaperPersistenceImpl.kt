package com.kapdroid.pomtom.database.repository

import app.cash.sqldelight.coroutines.asFlow
import com.kapdroid.pomtom.common.IdGenerator
import com.kapdroid.pomtom.database.PomtomDatabase
import com.kapdroid.pomtom.database.internal.toDomain
import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.entity.WallpaperSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WallpaperPersistence(
    private val database: PomtomDatabase,
    private val idGenerator: IdGenerator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val q get() = database.wallpaperQueries

    fun observeAll(): Flow<List<Wallpaper>> =
        q.selectAll().asFlow().map { it.executeAsList().map { row -> row.toDomain() } }.flowOn(dispatcher)

    fun observeUser(): Flow<List<Wallpaper>> =
        q.selectUser().asFlow().map { it.executeAsList().map { row -> row.toDomain() } }.flowOn(dispatcher)

    suspend fun getById(id: String): Wallpaper? = withContext(dispatcher) {
        q.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun upsert(
        id: String? = null,
        displayName: String,
        localPath: String,
        source: WallpaperSource,
        addedAtMs: Long,
    ): Wallpaper = withContext(dispatcher) {
        val resolvedId = id ?: idGenerator.newId()
        q.insert(
            id = resolvedId,
            display_name = displayName,
            local_path = localPath,
            source = source.name,
            added_at = addedAtMs,
        )
        Wallpaper(resolvedId, displayName, localPath, source, addedAtMs)
    }

    suspend fun delete(id: String) = withContext(dispatcher) {
        q.deleteById(id)
    }
}
