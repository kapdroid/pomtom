package com.kapdroid.pomtom.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.foundation.clickable
import com.kapdroid.pomtom.audio.presentation.AudioMixerScreen
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.goals.navigation.GoalsListRoute
import com.kapdroid.pomtom.goals.navigation.NewGoalRoute
import com.kapdroid.pomtom.goals.navigation.goalsGraph
import com.kapdroid.pomtom.onboarding.presentation.OnboardingScreen
import com.kapdroid.pomtom.settings.presentation.SettingsScreen
import com.kapdroid.pomtom.stats.navigation.StatsHomeRoute
import com.kapdroid.pomtom.stats.navigation.statsGraph
import com.kapdroid.pomtom.timer.navigation.TimerHomeRoute
import com.kapdroid.pomtom.timer.navigation.TimerSessionRoute
import com.kapdroid.pomtom.timer.navigation.TimerStrictRoute
import com.kapdroid.pomtom.timer.navigation.timerGraph
import com.kapdroid.pomtom.timer.presentation.CelebrateScreen
import kotlinx.serialization.Serializable

@Serializable
private data object AppRoot

@Composable
fun AppNavHost(startDestination: Any = TimerTabRoute) {
    val navController: NavHostController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val showShell = shouldShowAppShell(currentDestination)

    Column(modifier = Modifier.fillMaxSize().background(PomtomTheme.colors.bg0)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
            ) {
                composable<OnboardingRoute> {
                    OnboardingScreen(
                        onComplete = {
                            navController.navigate(TimerTabRoute) {
                                popUpTo(OnboardingRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable<TimerTabRoute> {
                    TimerTabHost(
                        onOpenSettings = { navController.navigate(SettingsRoute) },
                        onOpenAudioMixer = { navController.navigate(AudioMixerRoute) },
                        onOpenGoals = { navController.navigate(GoalsTabRoute) },
                        onCelebrate = { sessionId, goalId ->
                            navController.navigate(CelebrateRoute(sessionId, goalId))
                        },
                    )
                }
                composable<GoalsTabRoute> {
                    GoalsTabHost(parent = navController)
                }
                composable<StatsTabRoute> { StatsTabHost() }
                composable<YouTabRoute> { ComingSoonScreen(title = "You") }
                composable<AudioMixerRoute> { AudioMixerScreen(onClose = { navController.popBackStack() }) }
                composable<SettingsRoute> { SettingsScreen(onBack = { navController.popBackStack() }) }
                composable<CelebrateRoute> { entry ->
                    val args: CelebrateRoute = entry.toRoute()
                    CelebrateScreen(
                        sessionId = args.sessionId,
                        goalId = args.goalId,
                        onClose = {
                            navController.popBackStack(TimerTabRoute, inclusive = false)
                        },
                        onSetNextGoal = {
                            navController.navigate(GoalsTabRoute) {
                                popUpTo(TimerTabRoute) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }
        if (showShell) {
            BottomNavBar(
                current = currentDestination,
                onSelect = { route ->
                    navController.navigate(route) {
                        popUpTo(TimerTabRoute) { inclusive = false; saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Composable
private fun TimerTabHost(
    onOpenSettings: () -> Unit,
    onOpenAudioMixer: () -> Unit,
    onOpenGoals: () -> Unit,
    onCelebrate: (String, String?) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = TimerHomeRoute) {
        timerGraph(
            navController = navController,
            onOpenSettings = onOpenSettings,
            onOpenAudioMixer = onOpenAudioMixer,
            onOpenGoals = onOpenGoals,
            onCelebrate = onCelebrate,
        )
    }
}

@Composable
private fun GoalsTabHost(parent: NavHostController) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = GoalsListRoute) {
        goalsGraph(
            navController = navController,
            onBack = { parent.popBackStack() },
        )
    }
}

@Composable
private fun StatsTabHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = StatsHomeRoute) {
        statsGraph(navController = navController)
    }
}

@Composable
private fun BottomNavBar(
    current: NavDestination?,
    onSelect: (Any) -> Unit,
) {
    val colors = PomtomTheme.colors
    val items = listOf(
        BottomNavItem("Timer", Icons.Outlined.AccessTime, TimerTabRoute) { current.matchesAny(TimerTabRoute, TimerHomeRoute, TimerSessionRoute) },
        BottomNavItem("Goals", Icons.Outlined.EmojiEvents, GoalsTabRoute) { current.matchesAny(GoalsTabRoute) },
        BottomNavItem("Stats", Icons.Outlined.BarChart, StatsTabRoute) { current.matchesAny(StatsTabRoute) },
        BottomNavItem("You", Icons.Outlined.Person, YouTabRoute) { current.matchesAny(YouTabRoute) },
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (colors.isDark) colors.bg1 else colors.surface)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            BottomNavTab(item = item, onClick = { onSelect(item.route) })
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val isSelected: () -> Boolean,
)

@Composable
private fun BottomNavTab(item: BottomNavItem, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val active = item.isSelected()
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(
                    brush = if (active) Brush.linearGradient(listOf(colors.amber, colors.ember))
                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (active) colors.onAccent else colors.ink2,
            )
        }
        Text(
            text = item.label,
            style = PomtomTheme.typography.caption,
            color = if (active) colors.ink else colors.ink3,
        )
    }
}

private fun NavDestination?.matchesAny(vararg routes: Any): Boolean {
    val dest = this ?: return false
    return routes.any { route -> dest.hasRoute(route::class) }
}

private fun shouldShowAppShell(destination: NavDestination?): Boolean {
    if (destination == null) return true
    val hidden = listOf(TimerSessionRoute, TimerStrictRoute, AudioMixerRoute, SettingsRoute, NewGoalRoute, OnboardingRoute)
    val celebrateHidden = destination.hasRoute(CelebrateRoute::class)
    return !celebrateHidden && hidden.none { destination.hasRoute(it::class) }
}

@Composable
private fun ComingSoonScreen(title: String) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg0)
            .padding(PaddingValues(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = PomtomTheme.typography.titleSerif, color = colors.ink)
            Text(text = "Coming next phase", style = PomtomTheme.typography.body, color = colors.ink3)
        }
    }
}
