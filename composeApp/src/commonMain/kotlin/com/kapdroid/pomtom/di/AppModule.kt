package com.kapdroid.pomtom.di

import com.kapdroid.pomtom.audio.di.audioFeatureModule
import com.kapdroid.pomtom.audio.di.audioPlatformModule
import com.kapdroid.pomtom.common.di.commonModule
import com.kapdroid.pomtom.database.di.databaseModule
import com.kapdroid.pomtom.database.di.databasePlatformModule
import com.kapdroid.pomtom.datastore.di.datastoreModule
import com.kapdroid.pomtom.datastore.di.datastorePlatformModule
import com.kapdroid.pomtom.goals.di.goalsModule
import com.kapdroid.pomtom.onboarding.di.onboardingModule
import com.kapdroid.pomtom.platform.di.platformControllerModule
import com.kapdroid.pomtom.settings.di.settingsModule
import com.kapdroid.pomtom.stats.di.statsModule
import com.kapdroid.pomtom.timer.di.timerModule
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    includes(
        commonModule,
        databaseModule,
        databasePlatformModule,
        datastoreModule,
        datastorePlatformModule,
        audioPlatformModule,
        audioFeatureModule,
        useCaseModule,
        platformModule,
        platformControllerModule,
        timerModule,
        goalsModule,
        settingsModule,
        statsModule,
        onboardingModule,
    )
}
