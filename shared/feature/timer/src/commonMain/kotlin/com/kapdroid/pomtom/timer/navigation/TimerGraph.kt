package com.kapdroid.pomtom.timer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kapdroid.pomtom.timer.presentation.HomeScreen
import com.kapdroid.pomtom.timer.presentation.SessionRunningScreen
import kotlinx.serialization.Serializable

@Serializable
data object TimerHomeRoute

@Serializable
data object TimerSessionRoute

/**
 * StrictScreen route. Lives at the **top-level** nav graph (registered in `AppNavHost`)
 * — not inside the timer tab's nested NavHost. Promoting it lets `shouldShowAppShell`
 * see it as the current outer destination and hide the bottom nav while a focus session
 * is running. If it stayed nested, the outer controller would always read TimerTabRoute
 * and the bottom nav would still show during focus.
 */
@Serializable
data object TimerStrictRoute

fun NavGraphBuilder.timerGraph(
    navController: NavController,
    onOpenSettings: () -> Unit,
    onOpenAudioMixer: () -> Unit,
    onOpenGoals: () -> Unit,
    onCreateGoal: () -> Unit,
    onOpenStrict: (sessionId: String) -> Unit,
    onCelebrate: (sessionId: String, goalId: String?) -> Unit,
) {
    composable<TimerHomeRoute> {
        HomeScreen(
            onOpenSession = { _ -> navController.navigate(TimerSessionRoute) },
            onOpenStrict = onOpenStrict,
            onOpenSettings = onOpenSettings,
            onOpenAudioMixer = onOpenAudioMixer,
            onOpenGoals = onOpenGoals,
            onCreateGoal = onCreateGoal,
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
}
