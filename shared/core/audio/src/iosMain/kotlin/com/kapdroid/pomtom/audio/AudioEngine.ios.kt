package com.kapdroid.pomtom.audio

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioTime
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

// Compose Resources packages bundled assets inside the framework bundle under
//   compose-resources/{path}
private const val COMPOSE_RESOURCES_BUNDLE_PREFIX = "compose-resources"

@OptIn(ExperimentalForeignApi::class)
actual class AudioEngine {

    private data class TrackHandle(
        val node: AVAudioPlayerNode,
        val file: AVAudioFile,
    )

    private val engine = AVAudioEngine()
    private val tracks = mutableMapOf<String, TrackHandle>()

    private val _state = MutableStateFlow<Map<String, TrackState>>(emptyMap())
    actual val state: StateFlow<Map<String, TrackState>> = _state.asStateFlow()

    private var sessionConfigured = false

    actual suspend fun loadTrack(trackId: String, source: AudioSource): Result<Unit> = runCatching {
        configureSession()
        unloadInternal(trackId)

        val url = source.toURL() ?: error("Resource not found: ${source.key}")
        val file = AVAudioFile(forReading = url, error = null)
            ?: error("Could not open audio file at ${url.absoluteString}")
        val node = AVAudioPlayerNode().apply { volume = 0.6f }

        engine.attachNode(node)
        engine.connect(node, engine.mainMixerNode, file.processingFormat)

        if (!engine.isRunning()) {
            engine.prepare()
            engine.startAndReturnError(null)
        }

        tracks[trackId] = TrackHandle(node = node, file = file)
        scheduleLoop(trackId)
        mergeState(trackId) { it.copy(isLoaded = true, errorMessage = null) }
    }.onFailure { throwable ->
        Napier.e(throwable) { "Failed to load track $trackId" }
        mergeState(trackId) {
            it.copy(isLoaded = false, errorMessage = throwable.message ?: "load_failed")
        }
    }

    actual fun setVolume(trackId: String, gain: Float) {
        val clamped = gain.coerceIn(0f, 1f)
        tracks[trackId]?.node?.volume = clamped
        mergeState(trackId) { it.copy(volume = clamped) }
    }

    actual fun play(trackId: String) {
        val handle = tracks[trackId] ?: return
        if (!engine.isRunning()) {
            engine.prepare()
            engine.startAndReturnError(null)
        }
        if (!handle.node.isPlaying()) handle.node.play()
        mergeState(trackId) { it.copy(isPlaying = true, errorMessage = null) }
    }

    actual fun pause(trackId: String) {
        tracks[trackId]?.node?.pause()
        mergeState(trackId) { it.copy(isPlaying = false) }
    }

    actual fun stop(trackId: String) {
        tracks[trackId]?.let { handle ->
            handle.node.stop()
            scheduleLoop(trackId)
        }
        mergeState(trackId) { it.copy(isPlaying = false) }
    }

    actual fun playAll() {
        if (tracks.isEmpty()) return
        if (!engine.isRunning()) {
            engine.prepare()
            engine.startAndReturnError(null)
        }
        tracks.forEach { (id, handle) ->
            if (!handle.node.isPlaying()) handle.node.play()
            mergeState(id) { it.copy(isPlaying = true, errorMessage = null) }
        }
    }

    actual fun pauseAll() {
        tracks.forEach { (id, handle) ->
            handle.node.pause()
            mergeState(id) { it.copy(isPlaying = false) }
        }
    }

    actual fun stopAll() {
        tracks.forEach { (id, handle) ->
            handle.node.stop()
            scheduleLoop(id)
            mergeState(id) { it.copy(isPlaying = false) }
        }
    }

    actual fun unload(trackId: String) {
        unloadInternal(trackId)
    }

    actual fun release() {
        tracks.keys.toList().forEach { unloadInternal(it) }
        if (engine.isRunning()) engine.stop()
        if (sessionConfigured) {
            runCatching {
                AVAudioSession.sharedInstance().setActive(false, error = null)
            }
            sessionConfigured = false
        }
    }

    private fun unloadInternal(trackId: String) {
        tracks.remove(trackId)?.let { handle ->
            handle.node.stop()
            engine.detachNode(handle.node)
        }
        _state.update { it - trackId }
    }

    private fun scheduleLoop(trackId: String) {
        val handle = tracks[trackId] ?: return
        handle.node.scheduleFile(
            file = handle.file,
            atTime = null as AVAudioTime?,
            completionHandler = {
                if (tracks[trackId] === handle) scheduleLoop(trackId)
            },
        )
    }

    private fun mergeState(trackId: String, transform: (TrackState) -> TrackState) {
        _state.update { current ->
            val existing = current[trackId] ?: TrackState(trackId = trackId)
            current + (trackId to transform(existing))
        }
    }

    private fun configureSession() {
        if (sessionConfigured) return
        runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(
                category = AVAudioSessionCategoryPlayback,
                withOptions = AVAudioSessionCategoryOptionMixWithOthers,
                error = null,
            )
            session.setActive(true, error = null)
            sessionConfigured = true
        }.onFailure {
            Napier.w(it) { "AVAudioSession configuration failed" }
        }
    }

    private fun AudioSource.toURL(): NSURL? = when (this) {
        is AudioSource.Bundled -> {
            val (name, ext) = resourcePath.substringAfterLast('/').let { fileName ->
                val dot = fileName.lastIndexOf('.')
                if (dot > 0) fileName.substring(0, dot) to fileName.substring(dot + 1) else fileName to ""
            }
            val nestedPath = resourcePath.substringBeforeLast('/', missingDelimiterValue = "")
            val subdirectory = if (nestedPath.isEmpty()) {
                COMPOSE_RESOURCES_BUNDLE_PREFIX
            } else {
                "$COMPOSE_RESOURCES_BUNDLE_PREFIX/$nestedPath"
            }
            NSBundle.mainBundle.URLForResource(name, withExtension = ext, subdirectory = subdirectory)
        }
        is AudioSource.Local -> NSURL.fileURLWithPath(filePath)
    }
}
