package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.fakes.FakeClock
import com.kapdroid.pomtom.domain.fakes.FakeGoalsRepository
import com.kapdroid.pomtom.domain.fakes.FakeSessionRepository
import com.kapdroid.pomtom.domain.fakes.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CompleteFocusSessionUseCaseTest {

    @Test
    fun `complete writes COMPLETED status and increments attached SESSIONS goal`() = runTest {
        val clock = FakeClock()
        val bus = EventBus()
        val settings = FakeSettingsRepository()
        val sessions = FakeSessionRepository()
        val goals = FakeGoalsRepository().apply {
            create("Read", GoalType.SESSIONS, 3, GoalAttachMode.NEXT_SESSION, GoalColor.AMBER, clock.nowMs)
        }
        val increment = IncrementGoalProgressUseCase(goals, bus, clock)
        val start = StartFocusSessionUseCase(sessions, goals, settings, bus, clock)
        val complete = CompleteFocusSessionUseCase(sessions, increment, bus, clock)

        val started = start()
        clock.advance(25 * 60 * 1000L)
        val finished = complete(started.id, started.plannedMs)

        assertEquals(SessionStatus.COMPLETED, finished.status)
        assertEquals(1, goals.getById("g1")!!.progress)
    }

    @Test
    fun `complete a non-FOCUS phase does not bump goal progress`() = runTest {
        val clock = FakeClock()
        val bus = EventBus()
        val settings = FakeSettingsRepository()
        val sessions = FakeSessionRepository()
        val goals = FakeGoalsRepository().apply {
            create("Read", GoalType.SESSIONS, 3, GoalAttachMode.NEXT_SESSION, GoalColor.AMBER, clock.nowMs)
        }
        val increment = IncrementGoalProgressUseCase(goals, bus, clock)
        val complete = CompleteFocusSessionUseCase(sessions, increment, bus, clock)

        val shortBreak = sessions.start(
            plannedMs = 5 * 60 * 1000L,
            phase = com.kapdroid.pomtom.domain.entity.SessionPhase.SHORT_BREAK,
            cycleIndex = 1,
            strictMode = false,
            goalId = "g1",
            startedAtMs = clock.nowMs,
        )
        val finished = complete(shortBreak.id, shortBreak.plannedMs)

        assertNotNull(finished)
        assertEquals(0, goals.getById("g1")!!.progress)
    }
}
