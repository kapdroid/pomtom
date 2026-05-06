package com.kapdroid.pomtom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.audio.AudioSource
import com.kapdroid.pomtom.audio.focus.FocusAudioController
import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import io.github.aakira.napier.Napier
import com.kapdroid.pomtom.designsystem.components.LocalWallpaperPath
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.theme.palettes.CarbonPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.DuskPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.ForestPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.MochaPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.PaperPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.RosePalette
import com.kapdroid.pomtom.designsystem.theme.palettes.SakuraPalette
import com.kapdroid.pomtom.di.appModule
import com.kapdroid.pomtom.di.platformKoinConfig
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import com.kapdroid.pomtom.domain.repository.WallpaperRepository
import com.kapdroid.pomtom.navigation.AppNavHost
import com.kapdroid.pomtom.navigation.OnboardingRoute
import com.kapdroid.pomtom.navigation.TimerTabRoute
import com.kapdroid.pomtom.platform.Haptics
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        platformKoinConfig()
        modules(appModule)
    }) {
        PlatformBootstrap {
            ThemedApp()
        }
    }
}

@Composable
private fun ThemedApp() {
    val settingsRepository: SettingsRepository = koinInject()
    val wallpaperRepository: WallpaperRepository = koinInject()
    val settings by remember(settingsRepository) { settingsRepository.observe() }
        .collectAsStateWithLifecycle(initialValue = com.kapdroid.pomtom.domain.entity.AppSettings.Defaults)

    val focusAudioController: FocusAudioController = koinInject()
    LaunchedEffect(focusAudioController) { focusAudioController.start() }

    val palette = when (settings.theme) {
        AppTheme.DUSK -> DuskPalette
        AppTheme.PAPER -> PaperPalette
        AppTheme.FOREST -> ForestPalette
        AppTheme.ROSE -> RosePalette
        AppTheme.MOCHA -> MochaPalette
        AppTheme.CARBON -> CarbonPalette
        AppTheme.SAKURA -> SakuraPalette
    }
    val wallpaperPath by produceState<String?>(initialValue = null, settings.wallpaperId, wallpaperRepository) {
        value = settings.wallpaperId?.let { wallpaperRepository.getById(it)?.localPath }
    }
    val startDestination by produceState<Any?>(initialValue = null, settingsRepository) {
        value = if (settingsRepository.current().firstRunComplete) TimerTabRoute else OnboardingRoute
    }
    PomtomTheme(colors = palette) {
        CompositionLocalProvider(LocalWallpaperPath provides wallpaperPath) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PomtomTheme.colors.bg0),
            ) {
                HapticsBridge()
                startDestination?.let { destination ->
                    AppNavHost(startDestination = destination)
                }
            }
        }
    }
}

// Bridges domain events into user-facing feedback: haptics on every lifecycle
// event, plus a one-shot completion chime when a focus session finishes. Kept
// at the App root so the feedback fires regardless of which screen is on top
// (the user might have already navigated away from Strict by the time the
// completion event lands — the chime should still play). The chime is scaled
// by the user's masterVolume so a 0-volume preference silences it cleanly.
@Composable
private fun HapticsBridge() {
    val eventBus: EventBus = koinInject()
    val haptics: Haptics = koinInject()
    val audioEngine: AudioEngine = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    LaunchedEffect(eventBus, haptics, audioEngine, settingsRepository) {
        Napier.d { "PomtomBridge: subscribed to event bus" }
        eventBus.events.collect { event ->
            Napier.d { "PomtomBridge: event = $event" }
            when (event) {
                is DomainEvent.SessionCompleted -> {
                    haptics.success()
                    val volume = settingsRepository.current().masterVolume
                    Napier.d { "PomtomBridge: SessionCompleted -> playOneShot(chime) at gain=$volume" }
                    audioEngine.playOneShot(
                        source = AudioSource.Bundled("files/audio/chime.mp3"),
                        gain = volume,
                    )
                }
                is DomainEvent.SessionAborted -> haptics.warning()
                is DomainEvent.GoalCompleted -> haptics.success()
                is DomainEvent.SessionStarted -> haptics.tap()
                else -> Unit
            }
        }
    }
}
