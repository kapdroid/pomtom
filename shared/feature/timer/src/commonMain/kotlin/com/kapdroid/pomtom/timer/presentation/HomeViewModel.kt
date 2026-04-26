package com.kapdroid.pomtom.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.domain.entity.AppSettings
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import com.kapdroid.pomtom.domain.usecase.ObserveActiveSessionUseCase
import com.kapdroid.pomtom.domain.usecase.StartFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.UpdateSessionConfigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class HomeUiState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings.Defaults,
    val activeSession: FocusSession? = null,
    val attachedGoal: Goal? = null,
    val durationOptions: List<Duration> = listOf(15.minutes, 25.minutes, 45.minutes, 90.minutes),
    val pendingFocus: Duration = 25.minutes,
    val ambient: AmbientStatus = AmbientStatus.Idle,
)

sealed interface HomeUiEvent {
    data class SelectDuration(val duration: Duration) : HomeUiEvent
    data object BeginFocus : HomeUiEvent
}

sealed interface HomeNavigation {
    data class OpenSession(val sessionId: String) : HomeNavigation
    data class OpenStrict(val sessionId: String) : HomeNavigation
}

class HomeViewModel(
    private val observeActiveSession: ObserveActiveSessionUseCase,
    private val startFocus: StartFocusSessionUseCase,
    private val updateSessionConfig: UpdateSessionConfigUseCase,
    private val settingsRepository: SettingsRepository,
    private val goalsRepository: GoalsRepository,
    private val audioEngine: AudioEngine,
    private val audioLibrary: AudioLibraryRepository,
) : ViewModel() {

    private val pendingFocus = MutableStateFlow(25.minutes)
    private val navigation = MutableStateFlow<HomeNavigation?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(
            settingsRepository.observe(),
            observeActiveSession(),
            goalsRepository.observeActive(),
            pendingFocus,
            observeAmbientStatus(audioEngine, audioLibrary, settingsRepository),
        ) { settings, session, goals, pending, ambient ->
            val attached = session?.goalId?.let { id -> goals.firstOrNull { it.id == id } }
                ?: goals.firstOrNull { it.attachMode == com.kapdroid.pomtom.domain.entity.GoalAttachMode.NEXT_SESSION }
            HomeUiState(
                isLoading = false,
                settings = settings,
                activeSession = session,
                attachedGoal = attached,
                pendingFocus = pending,
                ambient = ambient,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState(),
        )

    val navigationEvents: StateFlow<HomeNavigation?> = navigation

    fun consumeNavigation() { navigation.value = null }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SelectDuration -> {
                pendingFocus.value = event.duration
                viewModelScope.launch {
                    val current = settingsRepository.current().sessionConfig
                    if (current.focus != event.duration) {
                        updateSessionConfig(current.copy(focus = event.duration))
                    }
                }
            }
            HomeUiEvent.BeginFocus -> viewModelScope.launch {
                val current = settingsRepository.current().sessionConfig
                val target = uiState.value.pendingFocus
                val effective: SessionConfig = if (current.focus != target) {
                    current.copy(focus = target).also { updateSessionConfig(it) }
                } else current
                val session = startFocus(phase = SessionPhase.FOCUS, cycleIndex = 0)
                navigation.value = if (effective.strictMode) {
                    HomeNavigation.OpenStrict(session.id)
                } else {
                    HomeNavigation.OpenSession(session.id)
                }
                pendingFocus.value = effective.focus
            }
        }
    }
}
