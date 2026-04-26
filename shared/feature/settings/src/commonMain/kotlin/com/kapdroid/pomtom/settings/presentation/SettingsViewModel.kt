package com.kapdroid.pomtom.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.common.Result
import com.kapdroid.pomtom.domain.entity.AppSettings
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.entity.CompanionType
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import com.kapdroid.pomtom.domain.repository.WallpaperRepository
import com.kapdroid.pomtom.domain.usecase.DeleteWallpaperUseCase
import com.kapdroid.pomtom.domain.usecase.ImportWallpaperUseCase
import com.kapdroid.pomtom.domain.usecase.SelectThemeUseCase
import com.kapdroid.pomtom.domain.usecase.SelectWallpaperUseCase
import com.kapdroid.pomtom.domain.usecase.SetCompanionUseCase
import com.kapdroid.pomtom.domain.usecase.ToggleStrictModeUseCase
import com.kapdroid.pomtom.domain.usecase.UpdateSessionConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class DurationField { FOCUS, SHORT_BREAK, LONG_BREAK, CYCLES_BEFORE_LONG }

data class SettingsUiState(
    val isLoading: Boolean = true,
    val sessionConfig: SessionConfig = SessionConfig(),
    val theme: AppTheme = AppTheme.Default,
    val wallpapers: List<Wallpaper> = emptyList(),
    val activeWallpaperId: String? = null,
    val isImportingWallpaper: Boolean = false,
    val wallpaperError: String? = null,
    val activeEditor: DurationField? = null,
    val companion: CompanionType = CompanionType.Default,
)

sealed interface SettingsUiEvent {
    data class OpenEditor(val field: DurationField) : SettingsUiEvent
    data object DismissEditor : SettingsUiEvent
    data class SetFocus(val duration: Duration) : SettingsUiEvent
    data class SetShortBreak(val duration: Duration) : SettingsUiEvent
    data class SetLongBreak(val duration: Duration) : SettingsUiEvent
    data class SetCyclesBeforeLong(val cycles: Int) : SettingsUiEvent
    data class SetStrictMode(val enabled: Boolean) : SettingsUiEvent
    data class SetTheme(val theme: AppTheme) : SettingsUiEvent
    data class SelectWallpaper(val id: String?) : SettingsUiEvent
    data class ImportWallpaper(val sourceUri: String, val displayName: String) : SettingsUiEvent
    data class DeleteWallpaper(val id: String) : SettingsUiEvent
    data object DismissWallpaperError : SettingsUiEvent
    data class WallpaperPickerError(val message: String?) : SettingsUiEvent
    data class SetCompanion(val companion: CompanionType) : SettingsUiEvent
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val updateSessionConfig: UpdateSessionConfigUseCase,
    private val toggleStrictMode: ToggleStrictModeUseCase,
    private val selectTheme: SelectThemeUseCase,
    private val selectWallpaper: SelectWallpaperUseCase,
    private val importWallpaper: ImportWallpaperUseCase,
    private val deleteWallpaper: DeleteWallpaperUseCase,
    private val setCompanion: SetCompanionUseCase,
) : ViewModel() {

    private val _activeEditor = MutableStateFlow<DurationField?>(null)
    private val _isImportingWallpaper = MutableStateFlow(false)
    private val _wallpaperError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsRepository.observe(),
            wallpaperRepository.observeAll(),
            _activeEditor,
            _isImportingWallpaper,
            _wallpaperError,
        ) { settings: AppSettings, wallpapers: List<Wallpaper>, editor: DurationField?, importing: Boolean, error: String? ->
            SettingsUiState(
                isLoading = false,
                sessionConfig = settings.sessionConfig,
                theme = settings.theme,
                wallpapers = wallpapers,
                activeWallpaperId = settings.wallpaperId,
                isImportingWallpaper = importing,
                wallpaperError = error,
                activeEditor = editor,
                companion = settings.companion,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsUiState(),
        )

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OpenEditor -> _activeEditor.value = event.field
            SettingsUiEvent.DismissEditor -> _activeEditor.value = null
            is SettingsUiEvent.SetFocus -> updateSession { it.copy(focus = event.duration) }
            is SettingsUiEvent.SetShortBreak -> updateSession { it.copy(shortBreak = event.duration) }
            is SettingsUiEvent.SetLongBreak -> updateSession { it.copy(longBreak = event.duration) }
            is SettingsUiEvent.SetCyclesBeforeLong -> updateSession { it.copy(cyclesBeforeLong = event.cycles) }
            is SettingsUiEvent.SetStrictMode -> viewModelScope.launch { toggleStrictMode(event.enabled) }
            is SettingsUiEvent.SetTheme -> viewModelScope.launch { selectTheme(event.theme) }
            is SettingsUiEvent.SelectWallpaper -> viewModelScope.launch { selectWallpaper(event.id) }
            is SettingsUiEvent.ImportWallpaper -> importWallpaperFlow(event.sourceUri, event.displayName)
            is SettingsUiEvent.DeleteWallpaper -> viewModelScope.launch { deleteWallpaper(event.id) }
            SettingsUiEvent.DismissWallpaperError -> _wallpaperError.value = null
            is SettingsUiEvent.WallpaperPickerError -> _wallpaperError.value = event.message ?: "Couldn’t pick image."
            is SettingsUiEvent.SetCompanion -> viewModelScope.launch { setCompanion(event.companion) }
        }
    }

    private fun updateSession(transform: (SessionConfig) -> SessionConfig) {
        viewModelScope.launch {
            val current = settingsRepository.current().sessionConfig
            val next = transform(current)
            if (next != current) updateSessionConfig(next)
            _activeEditor.value = null
        }
    }

    private fun importWallpaperFlow(sourceUri: String, displayName: String) {
        viewModelScope.launch {
            _isImportingWallpaper.value = true
            _wallpaperError.value = null
            val result = importWallpaper(sourceUri, displayName)
            _isImportingWallpaper.value = false
            when (result) {
                is Result.Success -> selectWallpaper(result.value.id)
                is Result.Failure -> _wallpaperError.value = result.error.message
            }
        }
    }

    companion object {
        val FocusOptions: List<Duration> = listOf(15.minutes, 25.minutes, 45.minutes, 60.minutes, 90.minutes)
        val ShortBreakOptions: List<Duration> = listOf(3.minutes, 5.minutes, 10.minutes, 15.minutes)
        val LongBreakOptions: List<Duration> = listOf(10.minutes, 15.minutes, 20.minutes, 30.minutes)
        val CycleOptions: List<Int> = listOf(2, 3, 4, 5, 6)
    }
}
