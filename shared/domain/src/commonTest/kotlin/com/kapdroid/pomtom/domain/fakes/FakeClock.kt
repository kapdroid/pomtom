package com.kapdroid.pomtom.domain.fakes

import com.kapdroid.pomtom.common.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FakeClock(initialMs: Long = 1_700_000_000_000L) : Clock {
    var nowMs: Long = initialMs
    private val zone = TimeZone.UTC

    fun advance(ms: Long) {
        nowMs += ms
    }

    override fun nowMs(): Long = nowMs
    override fun nowInstant(): Instant = Instant.fromEpochMilliseconds(nowMs)
    override fun timeZone(): TimeZone = zone
    override fun today(): LocalDate = nowInstant().toLocalDateTime(zone).date
}
