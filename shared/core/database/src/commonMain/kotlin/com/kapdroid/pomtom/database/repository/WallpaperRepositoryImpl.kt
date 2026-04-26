package com.kapdroid.pomtom.database.repository

import com.kapdroid.pomtom.domain.entity.BundledWallpapers
import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.entity.WallpaperSource
import com.kapdroid.pomtom.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WallpaperRepositoryImpl(
    private val persistence: WallpaperPersistence,
) : WallpaperRepository {

    /**
     * Bundled entries are prepended on every emission so the picker always shows them
     * first in a stable order. We use [WallpaperPersistence.observeUser] (not
     * `observeAll`) because the wallpaper table only ever holds USER rows — bundled
     * ones live in code, not the database.
     */
    override fun observeAll(): Flow<List<Wallpaper>> =
        persistence.observeUser().map { user -> BundledWallpapers.all + user }

    override suspend fun getById(id: String): Wallpaper? =
        BundledWallpapers.all.firstOrNull { it.id == id } ?: persistence.getById(id)

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

    /**
     * Refuses to delete bundled wallpapers — their existence is compile-time, not user
     * data. The Settings UI also hides the delete affordance on bundled tiles, so this
     * is defensive belt-and-braces.
     */
    override suspend fun deleteUser(id: String) {
        if (BundledWallpapers.all.any { it.id == id }) return
        persistence.delete(id)
    }
}
