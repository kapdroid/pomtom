package com.kapdroid.pomtom.domain.entity

enum class AppTheme(val displayName: String) {
    DUSK("Dusk"),
    PAPER("Paper"),
    FOREST("Forest"),
    ROSE("Rose");

    companion object {
        // Paper is the default — light cream surface that matches the launcher icon
        // background and the splash screen for a continuous "first launch" feel.
        val Default = PAPER
        fun fromIdOrDefault(id: String?): AppTheme = entries.firstOrNull { it.name == id } ?: Default
    }
}

enum class MotionIntensity { CALM, STANDARD, EXPRESSIVE }
