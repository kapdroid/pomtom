package com.kapdroid.pomtom.domain.entity

enum class AppTheme(val displayName: String) {
    DUSK("Dusk"),
    PAPER("Paper"),
    FOREST("Forest"),
    ROSE("Rose");

    companion object {
        val Default = DUSK
        fun fromIdOrDefault(id: String?): AppTheme = entries.firstOrNull { it.name == id } ?: Default
    }
}

enum class MotionIntensity { CALM, STANDARD, EXPRESSIVE }
