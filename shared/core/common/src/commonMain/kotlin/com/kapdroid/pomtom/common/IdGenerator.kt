package com.kapdroid.pomtom.common

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.random.Random

interface IdGenerator {
    fun newId(): String
}

class UuidIdGenerator(private val random: Random = Random.Default) : IdGenerator {
    override fun newId(): String {
        val bytes = ByteArray(16).also(random::nextBytes)
        bytes[6] = (bytes[6] and 0x0f) or 0x40
        bytes[8] = (bytes[8] and 0x3f) or 0x80.toByte()
        val hex = bytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        return buildString(36) {
            append(hex, 0, 8); append('-')
            append(hex, 8, 12); append('-')
            append(hex, 12, 16); append('-')
            append(hex, 16, 20); append('-')
            append(hex, 20, 32)
        }
    }
}
