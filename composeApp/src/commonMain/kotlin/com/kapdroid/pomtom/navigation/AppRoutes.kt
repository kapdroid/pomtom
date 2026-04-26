package com.kapdroid.pomtom.navigation

import kotlinx.serialization.Serializable

@Serializable
data object TimerTabRoute

@Serializable
data object GoalsTabRoute

@Serializable
data object StatsTabRoute

@Serializable
data object YouTabRoute

@Serializable
data object AudioMixerRoute

@Serializable
data object SettingsRoute

@Serializable
data class CelebrateRoute(val sessionId: String, val goalId: String?)

@Serializable
data object OnboardingRoute
