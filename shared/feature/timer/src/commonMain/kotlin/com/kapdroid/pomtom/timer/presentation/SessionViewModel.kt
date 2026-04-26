package com.kapdroid.pomtom.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapdroid.pomtom.audio.AudioEngine
import com.kapdroid.pomtom.common.Clock
import com.kapdroid.pomtom.domain.entity.CompanionType
import com.kapdroid.pomtom.domain.entity.FocusSession
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.AudioLibraryRepository
import com.kapdroid.pomtom.domain.repository.GoalsRepository
import com.kapdroid.pomtom.domain.repository.SettingsRepository
import com.kapdroid.pomtom.domain.usecase.AbortFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.CompleteFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.ObserveActiveSessionUseCase
import com.kapdroid.pomtom.domain.usecase.PauseFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.ResumeFocusSessionUseCase
import com.kapdroid.pomtom.domain.usecase.StartFocusSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CycleDot(
    val phase: SessionPhase,
    val index: Int,
    val isActive: Boolean,
    val isCompleted: Boolean,
)

data class SessionUiState(
    val session: FocusSession? = null,
    val attachedGoal: Goal? = null,
    val sessionConfig: SessionConfig = SessionConfig(),
    val nowMs: Long = 0L,
    val isLoading: Boolean = true,
    val ambient: AmbientStatus = AmbientStatus.Idle,
    val companion: CompanionType = CompanionType.Default,
) {
    val remainingMs: Long get() = session?.let {
        when (it.status) {
            SessionStatus.RUNNING -> (it.plannedMs - (it.actualMs + (nowMs - it.resumedAtMs).coerceAtLeast(0L))).coerceAtLeast(0L)
            SessionStatus.PAUSED -> (it.plannedMs - it.actualMs).coerceAtLeast(0L)
            else -> 0L
        }
    } ?: 0L

    val elapsedMs: Long get() = session?.let {
        when (it.status) {
            SessionStatus.RUNNING -> it.actualMs + (nowMs - it.resumedAtMs).coerceAtLeast(0L)
            else -> it.actualMs
        }
    } ?: 0L

    val progress: Float get() = session?.let {
        if (it.plannedMs <= 0) 0f else (elapsedMs.toFloat() / it.plannedMs.toFloat()).coerceIn(0f, 1f)
    } ?: 0f

    val cycleDots: List<CycleDot> get() {
        val current = session ?: return emptyList()
        val total = sessionConfig.cyclesBeforeLong
        return (0 until total).map { idx ->
            val cycleStart = idx * 2
            CycleDot(
                phase = sessionConfig.phaseAt(cycleStart),
                index = idx,
                isActive = current.phase == SessionPhase.FOCUS && (current.cycleIndex / 2) == idx,
                isCompleted = (current.cycleIndex / 2) > idx,
            )
        }
    }

    val isPaused: Boolean get() = session?.status == SessionStatus.PAUSED
    val isRunning: Boolean get() = session?.status == SessionStatus.RUNNING
    val isFinished: Boolean get() = session?.status == SessionStatus.COMPLETED ||
        session?.status == SessionStatus.ABORTED ||
        (session != null && remainingMs == 0L && session.status == SessionStatus.RUNNING)
}

sealed interface SessionUiEvent {
    data object Pause : SessionUiEvent
    data object Resume : SessionUiEvent
    data object Stop : SessionUiEvent
    data object Skip : SessionUiEvent
}

sealed interface SessionNavigation {
    data object Back : SessionNavigation
    data class Celebrate(val sessionId: String, val goalId: String?) : SessionNavigation
}

class SessionViewModel(
    private val observeActiveSession: ObserveActiveSessionUseCase,
    private val pauseSession: PauseFocusSessionUseCase,
    private val resumeSession: ResumeFocusSessionUseCase,
    private val completeSession: CompleteFocusSessionUseCase,
    private val abortSession: AbortFocusSessionUseCase,
    private val startSession: StartFocusSessionUseCase,
    private val settingsRepository: SettingsRepository,
    private val goalsRepository: GoalsRepository,
    private val ticker: SessionTickerService,
    private val clock: Clock,
    private val audioEngine: AudioEngine,
    private val audioLibrary: AudioLibraryRepository,
) : ViewModel() {

    private val nowMs = MutableStateFlow(clock.nowMs())
    private val navigation = MutableStateFlow<SessionNavigation?>(null)
    private var autoCompleted = false

    init {
        viewModelScope.launch {
            ticker.ticks().collect { nowMs.value = it }
        }
        viewModelScope.launch {
            observeActiveSession().collect { session ->
                if (session != null && session.status == SessionStatus.RUNNING && !autoCompleted) {
                    val elapsed = ticker.computeElapsedMs(session, clock.nowMs())
                    if (elapsed >= session.plannedMs) {
                        autoCompleted = true
                        finishSession(session, elapsed.coerceAtMost(session.plannedMs))
                    }
                }
                if (session == null) autoCompleted = false
            }
        }
    }

    val uiState: StateFlow<SessionUiState> =
        combine(
            observeActiveSession(),
            settingsRepository.observe(),
            goalsRepository.observeAll(),
            nowMs,
            observeAmbientStatus(audioEngine, audioLibrary, settingsRepository),
        ) { session, settings, goals, time, ambient ->
            val goal = session?.goalId?.let { id -> goals.firstOrNull { it.id == id } }
            SessionUiState(
                session = session,
                attachedGoal = goal,
                sessionConfig = settings.sessionConfig,
                nowMs = time,
                isLoading = false,
                ambient = ambient,
                companion = settings.companion,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SessionUiState(),
        )

    val navigationEvents: StateFlow<SessionNavigation?> = navigation
    fun consumeNavigation() { navigation.value = null }

    fun onEvent(event: SessionUiEvent) {
        val state = uiState.value
        val session = state.session ?: return
        when (event) {
            SessionUiEvent.Pause -> if (session.status == SessionStatus.RUNNING) {
                viewModelScope.launch {
                    pauseSession(session.id, state.elapsedMs)
                }
            }
            SessionUiEvent.Resume -> if (session.status == SessionStatus.PAUSED) {
                viewModelScope.launch { resumeSession(session.id) }
            }
            SessionUiEvent.Stop -> viewModelScope.launch {
                abortSession(session.id, state.elapsedMs)
                navigation.value = SessionNavigation.Back
            }
            SessionUiEvent.Skip -> viewModelScope.launch {
                val actual = state.elapsedMs
                completeSession(session.id, actual)
                advancePhase(session, state.sessionConfig)
            }
        }
    }

    private suspend fun finishSession(session: FocusSession, actualMs: Long) {
        completeSession(session.id, actualMs)
        if (session.phase == SessionPhase.FOCUS) {
            navigation.value = SessionNavigation.Celebrate(session.id, session.goalId)
        } else {
            advancePhase(session, settingsRepository.current().sessionConfig)
        }
    }

    private suspend fun advancePhase(prev: FocusSession, config: SessionConfig) {
        val nextIndex = prev.cycleIndex + 1
        val nextPhase = config.phaseAt(nextIndex)
        if (prev.phase != SessionPhase.FOCUS || nextPhase == SessionPhase.FOCUS) {
            // When focus completes we celebrate first; for other transitions just queue the next phase.
            startSession(phase = nextPhase, cycleIndex = nextIndex)
        }
    }
}
