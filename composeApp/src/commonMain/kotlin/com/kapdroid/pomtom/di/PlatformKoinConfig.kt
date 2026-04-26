package com.kapdroid.pomtom.di

import org.koin.core.KoinApplication

/**
 * Platform-specific configuration applied to the [KoinApplication] before modules are loaded.
 * On Android this registers the application [android.content.Context] via `androidContext()` so
 * platform modules (database, datastore, audio engine, file picker) can resolve it.
 * On iOS this is a no-op.
 */
expect fun KoinApplication.platformKoinConfig()
