package com.kapdroid.pomtom.common

import kotlinx.datetime.Clock as KxClock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface Clock {
    fun nowMs(): Long
    fun nowInstant(): Instant
    fun timeZone(): TimeZone
    fun today(): LocalDate
}

class SystemClock(
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : Clock {
    override fun nowMs(): Long = KxClock.System.now().toEpochMilliseconds()
    override fun nowInstant(): Instant = KxClock.System.now()
    override fun timeZone(): TimeZone = zone
    override fun today(): LocalDate = KxClock.System.now().toLocalDateTime(zone).date
}
