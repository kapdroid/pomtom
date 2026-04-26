package com.kapdroid.pomtom.stats.di

import com.kapdroid.pomtom.stats.presentation.HistoryViewModel
import com.kapdroid.pomtom.stats.presentation.StatsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val statsModule = module {
    viewModel {
        StatsViewModel(
            statsRepository = get(),
            sessionRepository = get(),
            goalsRepository = get(),
        )
    }
    viewModel {
        HistoryViewModel(
            sessionRepository = get(),
            goalsRepository = get(),
            clock = get(),
        )
    }
}
