package com.kapdroid.pomtom.database.di

import com.kapdroid.pomtom.database.DatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databasePlatformModule: Module = module {
    single { DatabaseFactory() }
}
