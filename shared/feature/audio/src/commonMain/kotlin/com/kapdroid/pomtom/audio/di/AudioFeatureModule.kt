package com.kapdroid.pomtom.audio.di

import com.kapdroid.pomtom.audio.data.AudioLibraryRepositoryImpl
import com.kapdroid.pomtom.audio.focus.FocusAudioController
import com.kapdroid.pomtom.audio.presentation.AudioMixerViewModel
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val audioFeatureModule = module {
    single<AudioLibraryRepository> {
        AudioLibraryRepositoryImpl(
            customStore = get(),
            audioProbe = get(),
        )
    }
    single {
        FocusAudioController(
            sessionRepository = get(),
            settingsRepository = get(),
            audioLibrary = get(),
            engine = get(),
        )
    }
    viewModel {
        AudioMixerViewModel(
            library = get(),
            importCustomAudio = get(),
            engine = get(),
            settingsRepository = get(),
        )
    }
}
