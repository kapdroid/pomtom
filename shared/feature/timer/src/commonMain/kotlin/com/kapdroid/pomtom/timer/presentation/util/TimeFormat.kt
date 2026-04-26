package com.kapdroid.pomtom.timer.presentation.util

import kotlin.math.floor

object TimeFormat {
    fun mmss(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return buildString {
            if (minutes < 10L) append('0')
            append(minutes)
            append(':')
            if (seconds < 10L) append('0')
            append(seconds)
        }
    }

    fun minutes(milliseconds: Long): Int = floor(milliseconds / 60_000.0).toInt()
}
