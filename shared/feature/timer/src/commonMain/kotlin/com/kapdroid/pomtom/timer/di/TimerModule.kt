package com.kapdroid.pomtom.timer.di

import com.kapdroid.pomtom.timer.presentation.CelebrateViewModel
import com.kapdroid.pomtom.timer.presentation.HomeViewModel
import com.kapdroid.pomtom.timer.presentation.SessionTickerService
import com.kapdroid.pomtom.timer.presentation.SessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val timerModule = module {
    single { SessionTickerService(clock = get()) }
    viewModel {
        HomeViewModel(
            observeActiveSession = get(),
            startFocus = get(),
            updateSessionConfig = get(),
            settingsRepository = get(),
            goalsRepository = get(),
            audioEngine = get(),
            audioLibrary = get(),
        )
    }
    viewModel {
        SessionViewModel(
            observeActiveSession = get(),
            pauseSession = get(),
            resumeSession = get(),
            completeSession = get(),
            abortSession = get(),
            startSession = get(),
            settingsRepository = get(),
            goalsRepository = get(),
            ticker = get(),
            clock = get(),
            audioEngine = get(),
            audioLibrary = get(),
        )
    }
    viewModel {
        CelebrateViewModel(
            goalsRepository = get(),
            sessionRepository = get(),
        )
    }
}
