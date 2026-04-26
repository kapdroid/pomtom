package com.kapdroid.pomtom.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes

class SessionConfigTest {

    @Test
    fun `phaseAt cycles focus and breaks correctly when cyclesBeforeLong is 2`() {
        val cfg = SessionConfig(cyclesBeforeLong = 2)
        assertEquals(SessionPhase.FOCUS, cfg.phaseAt(0))
        assertEquals(SessionPhase.SHORT_BREAK, cfg.phaseAt(1))
        assertEquals(SessionPhase.FOCUS, cfg.phaseAt(2))
        assertEquals(SessionPhase.LONG_BREAK, cfg.phaseAt(3))
        assertEquals(SessionPhase.FOCUS, cfg.phaseAt(4))
    }

    @Test
    fun `focus duration below 1 minute is rejected`() {
        assertFailsWith<IllegalArgumentException> { SessionConfig(focus = 30.minutes - 30.minutes) }
    }
}
