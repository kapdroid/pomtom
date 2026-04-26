package com.kapdroid.pomtom.timer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kapdroid.pomtom.timer.presentation.HomeScreen
import com.kapdroid.pomtom.timer.presentation.SessionRunningScreen
import com.kapdroid.pomtom.timer.presentation.StrictScreen
import kotlinx.serialization.Serializable

@Serializable
data object TimerHomeRoute

@Serializable
data object TimerSessionRoute

@Serializable
data object TimerStrictRoute

fun NavGraphBuilder.timerGraph(
    navController: NavController,
    onOpenSettings: () -> Unit,
    onOpenAudioMixer: () -> Unit,
    onOpenGoals: () -> Unit,
    onCelebrate: (sessionId: String, goalId: String?) -> Unit,
) {
    composable<TimerHomeRoute> {
        HomeScreen(
            onOpenSession = { _ -> navController.navigate(TimerSessionRoute) },
            onOpenStrict = { _ -> navController.navigate(TimerStrictRoute) },
            onOpenSettings = onOpenSettings,
            onOpenAudioMixer = onOpenAudioMixer,
            onOpenGoals = onOpenGoals,
        )
    }
    composable<TimerSessionRoute> {
        SessionRunningScreen(
            onBack = {
                if (!navController.popBackStack()) navController.navigate(TimerHomeRoute)
            },
            onCelebrate = onCelebrate,
            onOpenAudioMixer = onOpenAudioMixer,
        )
    }
    composable<TimerStrictRoute> {
        StrictScreen(
            onExit = {
                if (!navController.popBackStack()) navController.navigate(TimerHomeRoute)
            },
            onCelebrate = onCelebrate,
        )
    }
}
