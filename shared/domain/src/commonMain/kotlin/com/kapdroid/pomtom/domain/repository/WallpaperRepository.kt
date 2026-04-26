package com.kapdroid.pomtom.domain.repository

import com.kapdroid.pomtom.domain.entity.Wallpaper
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {
    fun observeAll(): Flow<List<Wallpaper>>
    suspend fun getById(id: String): Wallpaper?
    suspend fun importUser(sourceUri: String, displayName: String, addedAtMs: Long): Wallpaper
    suspend fun deleteUser(id: String)
}
