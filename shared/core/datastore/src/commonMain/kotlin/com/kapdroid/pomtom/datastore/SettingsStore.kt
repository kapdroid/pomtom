package com.kapdroid.pomtom.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kapdroid.pomtom.domain.entity.AppSettings
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.entity.CompanionType
import com.kapdroid.pomtom.domain.entity.MotionIntensity
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

const val SETTINGS_DATASTORE_FILE = "pomtom.preferences_pb"

fun createSettingsDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

private object Keys {
    val FOCUS_MS = longPreferencesKey("focus_ms")
    val SHORT_MS = longPreferencesKey("short_break_ms")
    val LONG_MS = longPreferencesKey("long_break_ms")
    val CYCLES = intPreferencesKey("cycles_before_long")
    val STRICT = booleanPreferencesKey("strict_mode")
    val THEME = stringPreferencesKey("theme")
    val WALLPAPER = stringPreferencesKey("wallpaper_id")
    val MOTION = stringPreferencesKey("motion")
    val MASTER_VOLUME = floatPreferencesKey("master_volume")
    val FIRST_RUN = booleanPreferencesKey("first_run_complete")
    val FOCUS_AUDIO_TRACK = stringPreferencesKey("focus_audio_track_id")
    val COMPANION = stringPreferencesKey("companion_type")
}

class SettingsStore(private val dataStore: DataStore<Preferences>) : SettingsRepository {

    override fun observe(): Flow<AppSettings> = dataStore.data.map { it.toSettings() }

    override suspend fun current(): AppSettings = dataStore.data.first().toSettings()

    override suspend fun setSessionConfig(config: SessionConfig) {
        dataStore.edit {
            it[Keys.FOCUS_MS] = config.focus.inWholeMilliseconds
            it[Keys.SHORT_MS] = config.shortBreak.inWholeMilliseconds
            it[Keys.LONG_MS] = config.longBreak.inWholeMilliseconds
            it[Keys.CYCLES] = config.cyclesBeforeLong
            it[Keys.STRICT] = config.strictMode
        }
    }

    override suspend fun setStrictMode(enabled: Boolean) {
        dataStore.edit { it[Keys.STRICT] = enabled }
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setWallpaperId(id: String?) {
        dataStore.edit {
            if (id == null) it.remove(Keys.WALLPAPER) else it[Keys.WALLPAPER] = id
        }
    }

    override suspend fun setMotion(motion: MotionIntensity) {
        dataStore.edit { it[Keys.MOTION] = motion.name }
    }

    override suspend fun setMasterVolume(volume: Float) {
        dataStore.edit { it[Keys.MASTER_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    override suspend fun setFirstRunComplete(value: Boolean) {
        dataStore.edit { it[Keys.FIRST_RUN] = value }
    }

    override suspend fun setFocusAudioTrackId(id: String?) {
        dataStore.edit {
            if (id == null) it.remove(Keys.FOCUS_AUDIO_TRACK) else it[Keys.FOCUS_AUDIO_TRACK] = id
        }
    }

    override suspend fun setCompanion(companion: CompanionType) {
        dataStore.edit { it[Keys.COMPANION] = companion.name }
    }

    private fun Preferences.toSettings(): AppSettings {
        val defaults = AppSettings.Defaults
        val focus = (this[Keys.FOCUS_MS] ?: defaults.focus.inWholeMilliseconds).milliseconds
        val short = (this[Keys.SHORT_MS] ?: defaults.shortBreak.inWholeMilliseconds).milliseconds
        val long = (this[Keys.LONG_MS] ?: defaults.longBreak.inWholeMilliseconds).milliseconds
        val cycles = this[Keys.CYCLES] ?: defaults.sessionConfig.cyclesBeforeLong
        val strict = this[Keys.STRICT] ?: defaults.sessionConfig.strictMode
        return AppSettings(
            sessionConfig = SessionConfig(
                focus = focus.coerceAtLeast(1.minutes),
                shortBreak = short.coerceAtLeast(1.minutes),
                longBreak = long.coerceAtLeast(1.minutes),
                cyclesBeforeLong = cycles.coerceIn(1, 10),
                strictMode = strict,
            ),
            theme = AppTheme.fromIdOrDefault(this[Keys.THEME]),
            wallpaperId = this[Keys.WALLPAPER],
            motion = this[Keys.MOTION]?.let { runCatching { MotionIntensity.valueOf(it) }.getOrNull() } ?: MotionIntensity.STANDARD,
            firstRunComplete = this[Keys.FIRST_RUN] ?: false,
            masterVolume = this[Keys.MASTER_VOLUME] ?: 1f,
            focusAudioTrackId = this[Keys.FOCUS_AUDIO_TRACK],
            companion = this[Keys.COMPANION]?.let { stored ->
                runCatching { CompanionType.valueOf(stored) }.getOrNull()
            } ?: CompanionType.Default,
        )
    }
}

private fun kotlin.time.Duration.coerceAtLeast(min: kotlin.time.Duration) = if (this < min) min else this
