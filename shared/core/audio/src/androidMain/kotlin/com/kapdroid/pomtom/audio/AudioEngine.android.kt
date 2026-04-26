package com.kapdroid.pomtom.audio

import android.content.Context
import android.media.AudioAttributes as PlatformAudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Compose Resources packages bundled assets under
//   assets/composeResources/{moduleQualifier}/{path}
// The qualifier matches the generated resources package of the consuming app.
private const val COMPOSE_RESOURCES_ASSET_PREFIX =
    "composeResources/pomtom.composeapp.generated.resources"

actual class AudioEngine(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val players = mutableMapOf<String, ExoPlayer>()
    private val _state = MutableStateFlow<Map<String, TrackState>>(emptyMap())
    actual val state: StateFlow<Map<String, TrackState>> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var hasFocus: Boolean = false
    private var serviceRunning: Boolean = false

    init {
        scope.launch {
            _state
                .map { snapshot -> snapshot.values.any(TrackState::isPlaying) }
                .distinctUntilChanged()
                .collect { anyPlaying -> updateForegroundService(anyPlaying) }
        }
    }

    private val mediaAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseAll()
            AudioManager.AUDIOFOCUS_GAIN -> {
                _state.value
                    .filterValues { it.isPlaying }
                    .keys
                    .forEach { resumePlayer(it) }
            }
        }
    }

    actual suspend fun loadTrack(trackId: String, source: AudioSource): Result<Unit> = runCatching {
        withContext(Dispatchers.Main) {
            unloadInternal(trackId)
            val player = ExoPlayer.Builder(context)
                .setAudioAttributes(mediaAttributes, /* handleAudioFocus = */ false)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0.6f
                    setMediaItem(toMediaItem(source))
                    addListener(playerListener(trackId))
                    prepare()
                }
            players[trackId] = player
            mergeState(trackId) { it.copy(isLoaded = true, errorMessage = null) }
        }
    }.onFailure { throwable ->
        Napier.e(throwable) { "Failed to load track $trackId" }
        mergeState(trackId) {
            it.copy(isLoaded = false, errorMessage = throwable.message ?: "load_failed")
        }
    }

    actual fun setVolume(trackId: String, gain: Float) {
        val clamped = gain.coerceIn(0f, 1f)
        runOnMain {
            players[trackId]?.volume = clamped
            mergeState(trackId) { it.copy(volume = clamped) }
        }
    }

    actual fun play(trackId: String) {
        runOnMain {
            val player = players[trackId] ?: return@runOnMain
            if (!ensureFocus()) return@runOnMain
            player.playWhenReady = true
            mergeState(trackId) { it.copy(isPlaying = true, errorMessage = null) }
        }
    }

    actual fun pause(trackId: String) {
        runOnMain {
            players[trackId]?.playWhenReady = false
            mergeState(trackId) { it.copy(isPlaying = false) }
            relinquishFocusIfIdle()
        }
    }

    actual fun stop(trackId: String) {
        runOnMain {
            players[trackId]?.let {
                it.playWhenReady = false
                it.seekTo(0L)
            }
            mergeState(trackId) { it.copy(isPlaying = false) }
            relinquishFocusIfIdle()
        }
    }

    actual fun playAll() {
        runOnMain {
            if (players.isEmpty() || !ensureFocus()) return@runOnMain
            players.forEach { (id, player) ->
                player.playWhenReady = true
                mergeState(id) { it.copy(isPlaying = true, errorMessage = null) }
            }
        }
    }

    actual fun pauseAll() {
        runOnMain {
            players.forEach { (id, player) ->
                player.playWhenReady = false
                mergeState(id) { it.copy(isPlaying = false) }
            }
            relinquishFocusIfIdle()
        }
    }

    actual fun stopAll() {
        runOnMain {
            players.forEach { (id, player) ->
                player.playWhenReady = false
                player.seekTo(0L)
                mergeState(id) { it.copy(isPlaying = false) }
            }
            relinquishFocusIfIdle()
        }
    }

    actual fun unload(trackId: String) {
        runOnMain { unloadInternal(trackId) }
    }

    actual fun release() {
        runOnMain {
            players.keys.toList().forEach { unloadInternal(it) }
            relinquishFocusIfIdle()
            if (serviceRunning) {
                PomtomAudioService.stop(context)
                serviceRunning = false
            }
        }
        scope.cancel()
    }

    private fun updateForegroundService(anyPlaying: Boolean) {
        if (anyPlaying && !serviceRunning) {
            PomtomAudioService.start(context)
            serviceRunning = true
        } else if (!anyPlaying && serviceRunning) {
            PomtomAudioService.stop(context)
            serviceRunning = false
        }
    }

    private fun unloadInternal(trackId: String) {
        players.remove(trackId)?.release()
        _state.update { current -> current - trackId }
    }

    private fun resumePlayer(trackId: String) {
        val player = players[trackId] ?: return
        if (!ensureFocus()) return
        player.playWhenReady = true
    }

    private fun toMediaItem(source: AudioSource): MediaItem = when (source) {
        // Compose Resources packages bundled assets under
        //   assets/composeResources/{moduleQualifier}/{path}
        // The qualifier comes from the consuming app's namespace.
        is AudioSource.Bundled -> MediaItem.fromUri(
            "asset:///$COMPOSE_RESOURCES_ASSET_PREFIX/${source.resourcePath}",
        )
        is AudioSource.Local -> MediaItem.fromUri(source.filePath)
    }

    private fun mergeState(trackId: String, transform: (TrackState) -> TrackState) {
        _state.update { current ->
            val existing = current[trackId] ?: TrackState(trackId = trackId)
            current + (trackId to transform(existing))
        }
    }

    private fun playerListener(trackId: String): Player.Listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            Napier.e(error) { "Playback error for $trackId" }
            mergeState(trackId) {
                it.copy(isPlaying = false, errorMessage = error.errorCodeName)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            mergeState(trackId) { it.copy(isPlaying = isPlaying) }
        }
    }

    private fun ensureFocus(): Boolean {
        if (hasFocus) return true
        val manager = audioManager ?: return true
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    PlatformAudioAttributes.Builder()
                        .setUsage(PlatformAudioAttributes.USAGE_MEDIA)
                        .setContentType(PlatformAudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(focusListener)
                .build()
                .also { focusRequest = it }
            manager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
        hasFocus = granted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasFocus
    }

    private fun relinquishFocusIfIdle() {
        if (!hasFocus) return
        val anyPlaying = _state.value.values.any { it.isPlaying }
        if (anyPlaying) return
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { manager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(focusListener)
        }
        hasFocus = false
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }
}
