package com.kapdroid.pomtom.goals.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kapdroid.pomtom.goals.presentation.GoalsScreen
import kotlinx.serialization.Serializable

@Serializable
data object GoalsListRoute

/**
 * The "create new goal" page. Lives at the **top-level** nav graph (registered in
 * `AppNavHost`), not inside the goals tab's nested NavHost — it's reachable from both
 * the goals list and the home screen's "no active goal" CTA, so a top-level route lets
 * either context navigate without depending on the other's nav controller.
 */
@Serializable
data object NewGoalRoute

fun NavGraphBuilder.goalsGraph(
    onBack: () -> Unit,
    onCreate: () -> Unit,
) {
    composable<GoalsListRoute> {
        GoalsScreen(
            onBack = onBack,
            onCreate = onCreate,
        )
    }
}
