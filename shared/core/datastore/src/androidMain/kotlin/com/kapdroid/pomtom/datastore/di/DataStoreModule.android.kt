package com.kapdroid.pomtom.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kapdroid.pomtom.datastore.SETTINGS_DATASTORE_FILE
import com.kapdroid.pomtom.datastore.createSettingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val datastorePlatformModule: Module = module {
    single<DataStore<Preferences>> {
        createSettingsDataStore {
            androidContext().filesDir.resolve(SETTINGS_DATASTORE_FILE).absolutePath
        }
    }
}
