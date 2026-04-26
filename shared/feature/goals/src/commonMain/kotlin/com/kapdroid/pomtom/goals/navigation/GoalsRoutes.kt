package com.kapdroid.pomtom.goals.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.kapdroid.pomtom.goals.presentation.GoalsScreen
import com.kapdroid.pomtom.goals.presentation.NewGoalScreen
import kotlinx.serialization.Serializable

@Serializable
data object GoalsListRoute

@Serializable
data object NewGoalRoute

fun NavGraphBuilder.goalsGraph(
    navController: NavHostController,
    onBack: () -> Unit,
) {
    composable<GoalsListRoute> {
        GoalsScreen(
            onBack = onBack,
            onCreate = { navController.navigate(NewGoalRoute) },
        )
    }
    composable<NewGoalRoute> {
        NewGoalScreen(onBack = { navController.popBackStack() })
    }
}
