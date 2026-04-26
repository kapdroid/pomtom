package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.fakes.FakeClock
import com.kapdroid.pomtom.domain.fakes.FakeGoalsRepository
import com.kapdroid.pomtom.domain.fakes.FakeSessionRepository
import com.kapdroid.pomtom.domain.fakes.FakeSettingsRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StartFocusSessionUseCaseTest {

    @Test
    fun `start writes RUNNING session with planned ms from settings`() = runTest {
        val clock = FakeClock()
        val bus = EventBus()
        val settings = FakeSettingsRepository()
        val sessions = FakeSessionRepository()
        val goals = FakeGoalsRepository()
        val useCase = StartFocusSessionUseCase(sessions, goals, settings, bus, clock)

        val session = useCase()

        assertEquals(SessionStatus.RUNNING, session.status)
        assertEquals(SessionPhase.FOCUS, session.phase)
        assertEquals(25 * 60 * 1000L, session.plannedMs)
        assertNull(session.goalId)
    }

    @Test
    fun `start auto-attaches a NEXT_SESSION goal when one exists`() = runTest {
        val clock = FakeClock()
        val bus = EventBus()
        val settings = FakeSettingsRepository()
        val sessions = FakeSessionRepository()
        val goals = FakeGoalsRepository().apply {
            create("Read book", GoalType.SESSIONS, 3, GoalAttachMode.NEXT_SESSION, GoalColor.AMBER, clock.nowMs)
        }
        val useCase = StartFocusSessionUseCase(sessions, goals, settings, bus, clock)

        val session = useCase()

        assertEquals("g1", session.goalId)
    }

    @Test
    fun `start emits SessionStarted on the event bus`() = runTest {
        val clock = FakeClock()
        val bus = EventBus()
        val settings = FakeSettingsRepository()
        val sessions = FakeSessionRepository()
        val goals = FakeGoalsRepository()
        val useCase = StartFocusSessionUseCase(sessions, goals, settings, bus, clock)

        val deferred = async(start = CoroutineStart.UNDISPATCHED) { bus.events.first() }
        useCase()
        val event = deferred.await()

        assertTrue(event is DomainEvent.SessionStarted)
    }
}
