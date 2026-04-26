package com.kapdroid.pomtom.datastore.di

import com.kapdroid.pomtom.datastore.SettingsStore
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val datastoreModule = module {
    single<SettingsRepository> { SettingsStore(get()) }
}

expect val datastorePlatformModule: Module
