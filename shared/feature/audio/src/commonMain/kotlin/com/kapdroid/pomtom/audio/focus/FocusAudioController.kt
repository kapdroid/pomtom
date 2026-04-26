package com.kapdroid.pomtom.audio.focus

import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.audio.AudioSource
import com.kapdroid.pomtom.domain.entity.AudioSourceKind
import com.kapdroid.pomtom.domain.entity.AudioTrack
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Coordinates ambient audio with the active focus session.
 *
 * Behaviour (transition-driven, not a continuous reconciler):
 *
 *  - When the active session enters `(FOCUS, RUNNING)` or the configured track changes
 *    while in that state, the configured track is loaded (if needed) and played.
 *  - When the session transitions to `PAUSED`, the configured track is paused.
 *  - When the session ends, transitions to a break phase, or the user clears their
 *    configured track, the configured track is stopped.
 *
 * The controller never fights manual playback: while a session is *not* in
 * `(FOCUS, RUNNING)`, the engine is free to be driven by the mixer screen
 * (preview playback). The single-track invariant lives in the engine and the
 * mixer ViewModel — this controller only acts on session transitions.
 *
 * Wire as a Koin `single`; the supplied [scope] outlives any individual screen
 * so the auto-play behaviour survives navigation.
 */
class FocusAudioController(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val audioLibrary: AudioLibraryRepository,
    private val engine: AudioEngine,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {

    private var collector: Job? = null
    private var armedTrackId: String? = null

    /**
     * Idempotent: starts the long-lived collector once. Safe to call from
     * multiple call sites (App composition, manifest bootstrap, etc.).
     */
    fun start() {
        if (collector?.isActive == true) return
        collector = scope.launch {
            combine(
                sessionRepository.observeActive()
                    .map { session ->
                        session?.let { Trigger(it.phase, it.status) }
                    }
                    .distinctUntilChanged(),
                settingsRepository.observe()
                    .map { it.focusAudioTrackId }
                    .distinctUntilChanged(),
                audioLibrary.observeAll(),
            ) { trigger, configuredId, library ->
                State(trigger = trigger, configuredId = configuredId, library = library)
            }.collect { state -> react(state) }
        }
    }

    fun stop() {
        collector?.cancel()
        collector = null
        armedTrackId?.let { engine.stop(it) }
        armedTrackId = null
    }

    private suspend fun react(state: State) {
        val trigger = state.trigger
        val configuredId = state.configuredId

        // No focus session, no configured track, or session already finished →
        // ensure nothing is armed.
        if (trigger == null || configuredId == null) {
            disarm()
            return
        }

        when (trigger.status) {
            SessionStatus.RUNNING -> if (trigger.phase == SessionPhase.FOCUS) {
                ensurePlaying(configuredId, state.library)
            } else {
                // Break phase — focus audio steps aside.
                disarm()
            }
            SessionStatus.PAUSED -> if (trigger.phase == SessionPhase.FOCUS) {
                pauseArmed(configuredId)
            } else {
                disarm()
            }
            SessionStatus.COMPLETED, SessionStatus.ABORTED -> disarm()
        }
    }

    private suspend fun ensurePlaying(trackId: String, library: List<AudioTrack>) {
        val track = library.firstOrNull { it.id == trackId }
        if (track == null) {
            Napier.w { "FocusAudioController: configured track $trackId not found in library" }
            disarm()
            return
        }
        if (armedTrackId != trackId) {
            // Stop whatever was armed before (different track or none).
            armedTrackId?.let { engine.stop(it) }
            val source = track.toAudioSource() ?: run {
                Napier.w { "FocusAudioController: track $trackId has no playable source" }
                disarm()
                return
            }
            val loaded = engine.loadTrack(trackId, source)
            if (loaded.isFailure) {
                Napier.w(loaded.exceptionOrNull()) { "FocusAudioController: failed to load $trackId" }
                armedTrackId = null
                return
            }
            armedTrackId = trackId
        }
        engine.play(trackId)
    }

    private fun pauseArmed(expectedId: String) {
        val armed = armedTrackId ?: return
        if (armed == expectedId) engine.pause(armed)
    }

    private fun disarm() {
        val armed = armedTrackId ?: return
        engine.stop(armed)
        armedTrackId = null
    }

    private data class Trigger(val phase: SessionPhase, val status: SessionStatus)
    private data class State(
        val trigger: Trigger?,
        val configuredId: String?,
        val library: List<AudioTrack>,
    )
}

private fun AudioTrack.toAudioSource(): AudioSource? = when (source) {
    AudioSourceKind.BUNDLED -> resourcePath?.let(AudioSource::Bundled)
    AudioSourceKind.CUSTOM -> localPath?.let(AudioSource::Local)
}
