package com.kapdroid.pomtom.domain.entity

enum class WallpaperSource { BUNDLED, USER }

data class Wallpaper(
    val id: String,
    val displayName: String,
    val localPath: String,
    val source: WallpaperSource,
    val addedAtMs: Long,
)
