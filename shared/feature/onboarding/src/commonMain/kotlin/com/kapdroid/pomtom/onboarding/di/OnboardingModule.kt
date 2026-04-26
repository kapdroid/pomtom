package com.kapdroid.pomtom.onboarding.di

import com.kapdroid.pomtom.onboarding.presentation.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    viewModel {
        OnboardingViewModel(
            completeOnboarding = get(),
        )
    }
}
