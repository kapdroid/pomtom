package com.kapdroid.pomtom.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.audio.AudioSource
import com.kapdroid.pomtom.audio.TrackState
import com.kapdroid.pomtom.domain.entity.AudioCategory
import com.kapdroid.pomtom.domain.entity.AudioSourceKind
import com.kapdroid.pomtom.domain.entity.AudioTrack
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import com.kapdroid.pomtom.domain.usecase.ImportCustomAudioUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AudioMixerUiState(
    val isLoading: Boolean = true,
    val tracks: List<TrackRow> = emptyList(),
    val errorMessage: String? = null,
    val isImporting: Boolean = false,
    val previewingName: String? = null,
    val focusTrackName: String? = null,
)

data class TrackRow(
    val track: AudioTrack,
    val state: TrackState,
    /** True if this row is the configured focus-session sound. */
    val isFocusAudio: Boolean = false,
) {
    val isActive: Boolean get() = state.isPlaying || state.volume > 0f && state.isLoaded
}

sealed interface AudioMixerEvent {
    /** Preview a track (single-track invariant: pauses any other preview first). */
    data class ToggleTrack(val trackId: String) : AudioMixerEvent
    data class SetVolume(val trackId: String, val gain: Float) : AudioMixerEvent
    data class DeleteCustom(val trackId: String) : AudioMixerEvent
    /**
     * Toggle this track as the focus-session sound. If it is already set, clears the
     * setting (no auto-play). Otherwise replaces whatever was previously configured.
     */
    data class ToggleFocusAudio(val trackId: String) : AudioMixerEvent
    data object DismissError : AudioMixerEvent
}

class AudioMixerViewModel(
    private val library: AudioLibraryRepository,
    private val importCustomAudio: ImportCustomAudioUseCase,
    private val engine: AudioEngine,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)
    private val importing = MutableStateFlow(false)

    /**
     * Track ids the user started playing from this screen ("previews"). Tracks
     * that the [com.kapdroid.pomtom.audio.focus.FocusAudioController] armed for
     * an active session are deliberately excluded — backing out of the mixer
     * must not silence a running focus session.
     */
    private val previewIds = mutableSetOf<String>()

    val uiState: StateFlow<AudioMixerUiState> = combine(
        library.observeAll(),
        engine.state,
        errorMessage,
        importing,
        settingsRepository.observe().map { it.focusAudioTrackId },
    ) { tracks, engineState, error, isImporting, focusTrackId ->
        val rows = tracks
            .sortedBy(AudioTrack::displayName)
            .map { track ->
                TrackRow(
                    track = track,
                    state = engineState[track.id] ?: TrackState(trackId = track.id),
                    isFocusAudio = focusTrackId != null && focusTrackId == track.id,
                )
            }
        AudioMixerUiState(
            isLoading = false,
            tracks = rows,
            errorMessage = error,
            isImporting = isImporting,
            previewingName = rows.firstOrNull { it.state.isPlaying }?.track?.displayName,
            focusTrackName = rows.firstOrNull { it.isFocusAudio }?.track?.displayName,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AudioMixerUiState(),
    )

    fun onEvent(event: AudioMixerEvent) {
        when (event) {
            is AudioMixerEvent.ToggleTrack -> toggleTrack(event.trackId)
            is AudioMixerEvent.SetVolume -> setVolume(event.trackId, event.gain)
            is AudioMixerEvent.DeleteCustom -> deleteCustom(event.trackId)
            is AudioMixerEvent.ToggleFocusAudio -> toggleFocusAudio(event.trackId)
            AudioMixerEvent.DismissError -> errorMessage.value = null
        }
    }

    private fun toggleFocusAudio(trackId: String) {
        viewModelScope.launch {
            val current = settingsRepository.current().focusAudioTrackId
            val next = if (current == trackId) null else trackId
            runCatching { settingsRepository.setFocusAudioTrackId(next) }
                .onFailure {
                    Napier.w(it) { "Could not update focus audio selection" }
                    errorMessage.value = it.message ?: "Could not save selection"
                }
        }
    }

    fun onAudioPicked(displayName: String, absolutePath: String) {
        if (importing.value) return
        importing.value = true
        viewModelScope.launch {
            val result = importCustomAudio(sourceUri = absolutePath, displayName = displayName)
            result.onFailureMessage { errorMessage.value = it }
            importing.value = false
        }
    }

    fun onPickerError(message: String?) {
        if (message.isNullOrBlank()) return
        errorMessage.value = message
    }

    private fun toggleTrack(trackId: String) {
        val rows = uiState.value.tracks
        val current = rows.firstOrNull { it.track.id == trackId } ?: return
        viewModelScope.launch {
            if (!current.state.isLoaded) {
                val source = current.track.toAudioSource()
                val loaded = engine.loadTrack(trackId, source)
                if (loaded.isFailure) {
                    errorMessage.value = loaded.exceptionOrNull()?.message ?: "Could not load track"
                    return@launch
                }
            }
            if (current.state.isPlaying) {
                engine.pause(trackId)
                previewIds.remove(trackId)
            } else {
                // Single-track policy: only one ambient track may play at a time.
                // Pause anything else still playing before starting the new one.
                rows.forEach { other ->
                    if (other.track.id != trackId && other.state.isPlaying) {
                        engine.pause(other.track.id)
                        previewIds.remove(other.track.id)
                    }
                }
                engine.play(trackId)
                previewIds.add(trackId)
            }
        }
    }

    /**
     * Pause anything the user previewed in this screen. Called from the
     * screen's `onDispose` so previews don't leak past navigation. Does not
     * touch the focus-session track armed by [FocusAudioController].
     */
    fun stopAllPreviews() {
        if (previewIds.isEmpty()) return
        previewIds.forEach { engine.pause(it) }
        previewIds.clear()
    }

    private fun setVolume(trackId: String, gain: Float) {
        engine.setVolume(trackId, gain)
    }

    private fun deleteCustom(trackId: String) {
        engine.unload(trackId)
        previewIds.remove(trackId)
        viewModelScope.launch {
            // Clear the focus selection first if it pointed at this track,
            // so the controller doesn't try to load a track that no longer exists.
            if (settingsRepository.current().focusAudioTrackId == trackId) {
                runCatching { settingsRepository.setFocusAudioTrackId(null) }
            }
            runCatching { library.deleteCustom(trackId) }
                .onFailure {
                    Napier.w(it) { "Could not delete custom track $trackId" }
                    errorMessage.value = it.message
                }
        }
    }

    companion object {
        fun groupByCategory(rows: List<TrackRow>): Map<AudioCategory, List<TrackRow>> =
            AudioCategory.entries.associateWith { category ->
                rows.filter { it.track.category == category }
            }.filterValues { it.isNotEmpty() }
    }
}

private fun AudioTrack.toAudioSource(): AudioSource = when (source) {
    AudioSourceKind.BUNDLED -> AudioSource.Bundled(
        resourcePath = resourcePath ?: error("bundled track missing resourcePath"),
    )
    AudioSourceKind.CUSTOM -> AudioSource.Local(
        filePath = localPath ?: error("custom track missing localPath"),
    )
}

private inline fun <T> com.kapdroid.pomtom.common.Result<T>.onFailureMessage(block: (String) -> Unit) {
    if (this is com.kapdroid.pomtom.common.Result.Failure) {
        block(error.message ?: "Something went wrong")
    }
}
