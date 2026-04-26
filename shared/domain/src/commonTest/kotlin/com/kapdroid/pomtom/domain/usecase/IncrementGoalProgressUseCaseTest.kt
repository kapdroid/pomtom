package com.kapdroid.pomtom.domain.usecase

import com.kapdroid.pomtom.common.DomainEvent
import com.kapdroid.pomtom.common.EventBus
import com.kapdroid.pomtom.common.ofType
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import com.kapdroid.pomtom.domain.fakes.FakeClock
import com.kapdroid.pomtom.domain.fakes.FakeGoalsRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IncrementGoalProgressUseCaseTest {

    @Test
    fun `MINUTES goal accumulates focused minutes`() = runTest {
        val clock = FakeClock()
        val goals = FakeGoalsRepository().apply {
            create("Deep work", GoalType.MINUTES, 60, GoalAttachMode.NEXT_SESSION, GoalColor.SAGE, clock.nowMs)
        }
        val useCase = IncrementGoalProgressUseCase(goals, EventBus(), clock)

        useCase("g1", focusedMs = 25 * 60 * 1000L)
        assertEquals(25, goals.getById("g1")!!.progress)
    }

    @Test
    fun `reaching the target marks goal completed and emits event`() = runTest {
        val clock = FakeClock()
        val bus = EventBus()
        val goals = FakeGoalsRepository().apply {
            create("Pages", GoalType.SESSIONS, 1, GoalAttachMode.NEXT_SESSION, GoalColor.AMBER, clock.nowMs)
        }
        val useCase = IncrementGoalProgressUseCase(goals, bus, clock)

        val deferred = async(start = CoroutineStart.UNDISPATCHED) { bus.events.ofType<DomainEvent.GoalCompleted>().first() }
        useCase("g1", focusedMs = 25 * 60 * 1000L)
        val event = deferred.await()

        assertEquals("g1", event.goalId)
        assertNotNull(goals.getById("g1")!!.completedAtMs)
    }

    @Test
    fun `progress contribution under one full unit is a no-op`() = runTest {
        val clock = FakeClock()
        val goals = FakeGoalsRepository().apply {
            create("Hours", GoalType.HOURS, 10, GoalAttachMode.NEXT_SESSION, GoalColor.VIOLET, clock.nowMs)
        }
        val useCase = IncrementGoalProgressUseCase(goals, EventBus(), clock)

        useCase("g1", focusedMs = 25 * 60 * 1000L)
        assertEquals(0, goals.getById("g1")!!.progress)
    }
}
