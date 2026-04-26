package com.kapdroid.pomtom.di

import com.kapdroid.pomtom.domain.usecase.AbortFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.CompleteFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.CompleteOnboardingUseCase
import com.kapdroid.pomtom.domain.usecase.CreateGoalUseCase
import com.kapdroid.pomtom.domain.usecase.DeleteGoalUseCase
import com.kapdroid.pomtom.domain.usecase.DeleteWallpaperUseCase
import com.kapdroid.pomtom.domain.usecase.ImportCustomAudioUseCase
import com.kapdroid.pomtom.domain.usecase.ImportWallpaperUseCase
import com.kapdroid.pomtom.domain.usecase.IncrementGoalProgressUseCase
import com.kapdroid.pomtom.domain.usecase.ObserveActiveSessionUseCase
import com.kapdroid.pomtom.domain.usecase.PauseFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.ResumeFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.SelectThemeUseCase
import com.kapdroid.pomtom.domain.usecase.SelectWallpaperUseCase
import com.kapdroid.pomtom.domain.usecase.SetGoalAttachModeUseCase
import com.kapdroid.pomtom.domain.usecase.StartFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.ToggleStrictModeUseCase
import com.kapdroid.pomtom.domain.usecase.UpdateSessionConfigUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { ObserveActiveSessionUseCase(get()) }
    factory { StartFocusSessionUseCase(get(), get(), get(), get(), get()) }
    factory { PauseFocusSessionUseCase(get(), get()) }
    factory { ResumeFocusSessionUseCase(get(), get()) }
    factory { IncrementGoalProgressUseCase(get(), get(), get()) }
    factory { CompleteFocusSessionUseCase(get(), get(), get(), get()) }
    factory { AbortFocusSessionUseCase(get(), get(), get()) }
    factory { CreateGoalUseCase(get(), get(), get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { SetGoalAttachModeUseCase(get()) }
    factory { ImportCustomAudioUseCase(get(), get(), get()) }
    factory { ImportWallpaperUseCase(get(), get(), get()) }
    factory { DeleteWallpaperUseCase(get(), get()) }
    factory { UpdateSessionConfigUseCase(get()) }
    factory { ToggleStrictModeUseCase(get()) }
    factory { SelectThemeUseCase(get(), get()) }
    factory { SelectWallpaperUseCase(get()) }
    factory { CompleteOnboardingUseCase(get()) }
}
