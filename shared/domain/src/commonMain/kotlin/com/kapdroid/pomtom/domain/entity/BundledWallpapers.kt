package com.kapdroid.pomtom.domain.entity

/**
 * The set of wallpapers shipped inside the app binary. These rows live entirely in code
 * — they are NOT seeded into the SQLite [com.kapdroid.pomtom.domain.repository.WallpaperRepository]
 * table. The repository merges this list with the user-imported entries on read, which
 * keeps two nice properties:
 *
 *  1. **No first-run seeding logic.** Adding/removing a bundled wallpaper is a code edit
 *     that ships with the next build — no migration, no "have we seeded?" flag, no risk
 *     of duplicate inserts on cold start.
 *  2. **No DB rows tied to compile-time strings.** The Compose Resources path is a build
 *     artifact; persisting it would let stale rows survive a renamed/removed asset.
 *
 * The [Wallpaper.localPath] for a bundled entry is the **resource path** (relative to
 * `composeResources/`), not a `file://` URI. The renderer (`AuroraBackground`) detects
 * this shape and runs it through `Res.getUri(...)` to get the platform-resolved URL that
 * Coil can load.
 *
 * IDs are prefixed `bundled.` so they cannot collide with user-imported IDs (which come
 * from [com.kapdroid.pomtom.common.IdGenerator] and use UUID-style strings).
 */
object BundledWallpapers {
    val Celestial: Wallpaper = Wallpaper(
        id = "bundled.celestial",
        displayName = "Celestial",
        localPath = "files/wallpapers/celestial.webp",
        source = WallpaperSource.BUNDLED,
        addedAtMs = 0L,
    )

    val Bulb: Wallpaper = Wallpaper(
        id = "bundled.bulb",
        displayName = "Lone bulb",
        localPath = "files/wallpapers/bulb.jpg",
        source = WallpaperSource.BUNDLED,
        addedAtMs = 0L,
    )

    /**
     * Display order in the picker. Curated, not chronological — put the moodiest one
     * first because the picker rail is read left-to-right.
     */
    val all: List<Wallpaper> = listOf(Celestial, Bulb)
}
