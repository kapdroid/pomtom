package com.kapdroid.pomtom.timer.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.components.AuroraBackground
import com.kapdroid.pomtom.designsystem.components.PrimaryGradientButton
import com.kapdroid.pomtom.designsystem.components.ProgressRing
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.timer.presentation.components.AmbientCard
import com.kapdroid.pomtom.timer.presentation.components.DurationPicker
import com.kapdroid.pomtom.timer.presentation.components.GoalCard
import com.kapdroid.pomtom.timer.presentation.components.GoalSuggestionCard
import com.kapdroid.pomtom.timer.presentation.util.TimeFormat
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenSession: (String) -> Unit,
    onOpenStrict: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAudioMixer: () -> Unit,
    onOpenGoals: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nav by viewModel.navigationEvents.collectAsStateWithLifecycle()
    LaunchedEffect(nav) {
        when (val target = nav) {
            is HomeNavigation.OpenSession -> {
                onOpenSession(target.sessionId)
                viewModel.consumeNavigation()
            }
            is HomeNavigation.OpenStrict -> {
                onOpenStrict(target.sessionId)
                viewModel.consumeNavigation()
            }
            null -> Unit
        }
    }
    AuroraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            HomeChrome(onOpenSettings = onOpenSettings)
            Spacer(Modifier.height(18.dp))
            if (state.attachedGoal != null) {
                GoalCard(state.attachedGoal!!)
            } else {
                GoalSuggestionCard(modifier = Modifier.semantics { contentDescription = "Add a goal" })
            }
            Spacer(Modifier.height(28.dp))
            TimerHero(
                pendingMinutes = state.pendingFocus.inWholeMinutes.toInt(),
                onBegin = { viewModel.onEvent(HomeUiEvent.BeginFocus) },
            )
            Spacer(Modifier.height(28.dp))
            DurationPicker(
                options = state.durationOptions,
                selected = state.pendingFocus,
                onSelected = { viewModel.onEvent(HomeUiEvent.SelectDuration(it)) },
            )
            Spacer(Modifier.height(20.dp))
            AmbientCard(
                label = state.ambient.label,
                isPlaying = state.ambient.isPlaying,
                hasChoice = state.ambient.hasChoice,
                onClick = onOpenAudioMixer,
            )
        }
    }
}

@Composable
private fun HomeChrome(onOpenSettings: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(percent = 50))
                .padding(2.dp)
                .clip(RoundedCornerShape(percent = 50))
                .padding(1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(percent = 50)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(percent = 50)),
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.linearGradient(listOf(colors.amber, colors.ember)),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Pomtom", style = PomtomTheme.typography.titleSans, color = colors.ink)
            Text("Tuesday · 3 of 4 cycles", style = PomtomTheme.typography.caption, color = colors.ink3)
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = colors.ink2,
            )
        }
    }
}

@Composable
private fun TimerHero(pendingMinutes: Int, onBegin: () -> Unit) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            ProgressRing(progress = 0f, isRunning = false) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ready",
                        style = PomtomTheme.typography.caption,
                        color = colors.ink3,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = TimeFormat.mmss((pendingMinutes * 60_000L).toLong()),
                        style = PomtomTheme.typography.timer,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$pendingMinutes minute focus",
                        style = PomtomTheme.typography.label,
                        color = colors.ink2,
                    )
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        PrimaryGradientButton(
            label = "Begin focus",
            onClick = onBegin,
            leadingIcon = Icons.Outlined.PlayArrow,
            modifier = Modifier.width(220.dp),
        )
    }
}
