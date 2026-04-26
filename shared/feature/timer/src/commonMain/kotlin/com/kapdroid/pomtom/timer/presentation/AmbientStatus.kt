package com.kapdroid.pomtom.timer.presentation

import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Three-state model for the ambient pill on Home / Session screens.
 *
 *  - **Idle**: nothing chosen, nothing playing. Prompt the user to pick.
 *  - **Chosen**: the user has set a focus sound, but it isn't playing right now
 *    (between sessions, or session is paused). Show the track name as a preview
 *    of "what will play".
 *  - **Playing**: a track is currently audible. Show its name.
 */
data class AmbientStatus(
    val label: String,
    val isPlaying: Boolean,
    val hasChoice: Boolean = false,
) {
    companion object {
        val Idle = AmbientStatus(label = "Tap to choose a sound", isPlaying = false, hasChoice = false)
    }
}

internal fun observeAmbientStatus(
    engine: AudioEngine,
    library: AudioLibraryRepository,
    settings: SettingsRepository,
): Flow<AmbientStatus> = combine(
    engine.state,
    library.observeAll(),
    settings.observe().map { it.focusAudioTrackId }.distinctUntilChanged(),
) { states, tracks, focusId ->
    // Single-track invariant: at most one entry should ever satisfy isPlaying.
    val playing = states.values.firstOrNull { it.isPlaying }
    if (playing != null) {
        val name = tracks.firstOrNull { it.id == playing.trackId }?.displayName ?: "Playing"
        return@combine AmbientStatus(label = name, isPlaying = true, hasChoice = true)
    }
    if (focusId != null) {
        val name = tracks.firstOrNull { it.id == focusId }?.displayName
        if (name != null) {
            return@combine AmbientStatus(label = name, isPlaying = false, hasChoice = true)
        }
        // Configured track has been deleted from the library — fall through to Idle.
    }
    AmbientStatus.Idle
}
