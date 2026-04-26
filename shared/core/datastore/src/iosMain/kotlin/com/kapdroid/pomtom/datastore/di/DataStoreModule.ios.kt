package com.kapdroid.pomtom.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kapdroid.pomtom.datastore.SETTINGS_DATASTORE_FILE
import com.kapdroid.pomtom.datastore.createSettingsDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val datastorePlatformModule: Module = module {
    single<DataStore<Preferences>> {
        createSettingsDataStore {
            val documents = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            ) as NSURL
            requireNotNull(documents.path) { "documents path missing" } + "/" + SETTINGS_DATASTORE_FILE
        }
    }
}
