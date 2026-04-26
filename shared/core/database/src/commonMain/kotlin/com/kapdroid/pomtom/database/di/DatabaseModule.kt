package com.kapdroid.pomtom.database.di

import com.kapdroid.pomtom.database.PomtomDatabase
import com.kapdroid.pomtom.database.createDatabase
import com.kapdroid.pomtom.database.repository.CustomAudioPersistence
import com.kapdroid.pomtom.database.repository.GoalsRepositoryImpl
import com.kapdroid.pomtom.database.repository.SessionRepositoryImpl
import com.kapdroid.pomtom.database.repository.StatsRepositoryImpl
import com.kapdroid.pomtom.database.repository.WallpaperPersistence
import com.kapdroid.pomtom.database.repository.WallpaperRepositoryImpl
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SessionRepository
import com.kapdroid.pomtom.domain.repository.StatsRepository
import com.kapdroid.pomtom.domain.repository.WallpaperRepository
import org.koin.dsl.module

val databaseModule = module {
    single<PomtomDatabase> { createDatabase(get()) }
    single<GoalsRepository> { GoalsRepositoryImpl(get(), get()) }
    single<SessionRepository> { SessionRepositoryImpl(get(), get()) }
    single<StatsRepository> { StatsRepositoryImpl(get(), get()) }
    single { WallpaperPersistence(get(), get()) }
    single<WallpaperRepository> { WallpaperRepositoryImpl(get()) }
    single { CustomAudioPersistence(get(), get()) }
}
