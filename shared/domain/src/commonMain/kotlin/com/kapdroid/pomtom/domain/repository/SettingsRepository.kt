package com.kapdroid.pomtom.domain.repository

import com.kapdroid.pomtom.domain.entity.AppSettings
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.entity.MotionIntensity
import com.kapdroid.pomtom.domain.entity.SessionConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun current(): AppSettings

    suspend fun setSessionConfig(config: SessionConfig)
    suspend fun setStrictMode(enabled: Boolean)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setWallpaperId(id: String?)
    suspend fun setMotion(motion: MotionIntensity)
    suspend fun setMasterVolume(volume: Float)
    suspend fun setFirstRunComplete(value: Boolean)
    suspend fun setFocusAudioTrackId(id: String?)
}
