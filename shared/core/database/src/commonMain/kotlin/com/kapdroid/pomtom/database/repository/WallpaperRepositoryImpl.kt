package com.kapdroid.pomtom.database.repository

import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.entity.WallpaperSource
import com.kapdroid.pomtom.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class WallpaperRepositoryImpl(
    private val persistence: WallpaperPersistence,
) : WallpaperRepository {

    override fun observeAll(): Flow<List<Wallpaper>> = persistence.observeAll()

    override suspend fun getById(id: String): Wallpaper? = persistence.getById(id)

    override suspend fun importUser(
        sourceUri: String,
        displayName: String,
        addedAtMs: Long,
    ): Wallpaper = persistence.upsert(
        displayName = displayName,
        localPath = sourceUri,
        source = WallpaperSource.USER,
        addedAtMs = addedAtMs,
    )

    override suspend fun deleteUser(id: String) = persistence.delete(id)
}
