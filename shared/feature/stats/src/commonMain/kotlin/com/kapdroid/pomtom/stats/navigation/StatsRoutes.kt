package com.kapdroid.pomtom.stats.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.kapdroid.pomtom.stats.presentation.HistoryScreen
import com.kapdroid.pomtom.stats.presentation.StatsScreen
import kotlinx.serialization.Serializable

@Serializable
data object StatsHomeRoute

@Serializable
data object HistoryRoute

fun NavGraphBuilder.statsGraph(navController: NavHostController) {
    composable<StatsHomeRoute> {
        StatsScreen(
            onOpenHistory = { navController.navigate(HistoryRoute) },
        )
    }
    composable<HistoryRoute> {
        HistoryScreen(onBack = { navController.popBackStack() })
    }
}
