package com.kapdroid.pomtom.goals.di

import com.kapdroid.pomtom.goals.presentation.GoalsViewModel
import com.kapdroid.pomtom.goals.presentation.NewGoalViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val goalsModule = module {
    viewModel {
        GoalsViewModel(
            goalsRepository = get(),
            setAttachMode = get(),
            deleteGoal = get(),
        )
    }
    viewModel {
        NewGoalViewModel(
            createGoal = get(),
        )
    }
}
