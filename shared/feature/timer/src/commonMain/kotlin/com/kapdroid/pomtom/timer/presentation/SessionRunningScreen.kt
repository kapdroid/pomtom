package com.kapdroid.pomtom.timer.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.components.AuroraBackground
import com.kapdroid.pomtom.designsystem.components.CompanionAssets
import com.kapdroid.pomtom.designsystem.components.EqIcon
import com.kapdroid.pomtom.designsystem.components.FocusCompanion
import com.kapdroid.pomtom.designsystem.components.ProgressRing
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.util.WithWindowMetrics
import com.kapdroid.pomtom.designsystem.util.softCard
import com.kapdroid.pomtom.domain.entity.CompanionType
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.timer.presentation.components.CycleDotsRow
import com.kapdroid.pomtom.timer.presentation.util.TimeFormat
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SessionRunningScreen(
    onBack: () -> Unit,
    onCelebrate: (sessionId: String, goalId: String?) -> Unit,
    onOpenAudioMixer: () -> Unit,
    viewModel: SessionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nav by viewModel.navigationEvents.collectAsStateWithLifecycle()
    LaunchedEffect(nav) {
        when (val target = nav) {
            SessionNavigation.Back -> {
                onBack()
                viewModel.consumeNavigation()
            }
            is SessionNavigation.Celebrate -> {
                onCelebrate(target.sessionId, target.goalId)
                viewModel.consumeNavigation()
            }
            null -> Unit
        }
    }
    LaunchedEffect(state.session) {
        if (!state.isLoading && state.session == null) onBack()
    }

    AuroraBackground {
        WithWindowMetrics { metrics ->
            if (metrics.isLandscape) {
                SessionContentLandscape(
                    state = state,
                    onPauseToggle = { viewModel.onEvent(if (state.isPaused) SessionUiEvent.Resume else SessionUiEvent.Pause) },
                    onSkip = { viewModel.onEvent(SessionUiEvent.Skip) },
                    onStop = { viewModel.onEvent(SessionUiEvent.Stop) },
                    onOpenAudioMixer = onOpenAudioMixer,
                )
            } else {
                SessionContentPortrait(
                    state = state,
                    onPauseToggle = { viewModel.onEvent(if (state.isPaused) SessionUiEvent.Resume else SessionUiEvent.Pause) },
                    onSkip = { viewModel.onEvent(SessionUiEvent.Skip) },
                    onStop = { viewModel.onEvent(SessionUiEvent.Stop) },
                    onOpenAudioMixer = onOpenAudioMixer,
                )
            }
        }
    }
}

@Composable
private fun SessionContentPortrait(
    state: SessionUiState,
    onPauseToggle: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onOpenAudioMixer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InSessionPill(isPaused = state.isPaused, isFocus = state.session?.phase == SessionPhase.FOCUS)
        Spacer(Modifier.height(20.dp))
        CycleDotsRow(dots = state.cycleDots)
        Spacer(Modifier.height(28.dp))
        SessionRing(state = state, diameter = 290.dp)
        Spacer(Modifier.height(28.dp))
        AmbientPeek(ambient = state.ambient, onClick = onOpenAudioMixer)
        SessionCompanionSlot(state = state, topPadding = 14.dp)
        Spacer(Modifier.weight(1f))
        ActionCluster(
            isPaused = state.isPaused,
            onPauseToggle = onPauseToggle,
            onSkip = onSkip,
            onStop = onStop,
        )
        Spacer(Modifier.height(8.dp))
    }
}

// Landscape layout: ring centered on the left half, the supporting elements (pill,
// cycle dots, ambient, companion, action cluster) stacked on the right half. The ring
// shrinks slightly (290 → 240 dp) to fit short heights without crowding the controls.
@Composable
private fun SessionContentLandscape(
    state: SessionUiState,
    onPauseToggle: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onOpenAudioMixer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SessionRing(state = state, diameter = 240.dp)
        }
        Spacer(Modifier.width(24.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            InSessionPill(isPaused = state.isPaused, isFocus = state.session?.phase == SessionPhase.FOCUS)
            CycleDotsRow(dots = state.cycleDots)
            AmbientPeek(ambient = state.ambient, onClick = onOpenAudioMixer)
            SessionCompanionSlot(state = state, topPadding = 0.dp)
            Spacer(Modifier.weight(1f))
            ActionCluster(
                isPaused = state.isPaused,
                onPauseToggle = onPauseToggle,
                onSkip = onSkip,
                onStop = onStop,
            )
        }
    }
}

// Extracted so both layouts share identical ring chrome (label / time / goal title).
@Composable
private fun SessionRing(state: SessionUiState, diameter: androidx.compose.ui.unit.Dp) {
    ProgressRing(
        progress = state.progress,
        isRunning = state.isRunning,
        diameter = diameter,
    ) {
        // fillMaxWidth + CenterHorizontally so each child centers on the ring's true
        // axis (otherwise the column sizes to its widest child and the time text drifts
        // off-center within the inner ring area).
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.session?.phase?.displayLabel() ?: "FOCUS",
                style = PomtomTheme.typography.caption,
                color = PomtomTheme.colors.ink3,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = TimeFormat.mmss(state.remainingMs),
                style = PomtomTheme.typography.timer.copy(lineHeight = PomtomTheme.typography.timer.fontSize),
                color = PomtomTheme.colors.ink,
            )
            Spacer(Modifier.height(6.dp))
            val attached = state.attachedGoal
            Text(
                text = attached?.title ?: "Pure focus",
                style = PomtomTheme.typography.label,
                color = PomtomTheme.colors.ink2,
            )
        }
    }
}

// Companion is FOCUS-only — extracted so both layouts agree on visibility rules.
@Composable
private fun SessionCompanionSlot(state: SessionUiState, topPadding: androidx.compose.ui.unit.Dp) {
    val asset = state.companion.assetPath()
    if (asset != null && state.session?.phase == SessionPhase.FOCUS) {
        if (topPadding > 0.dp) Spacer(Modifier.height(topPadding))
        FocusCompanion(
            assetPath = asset,
            contentDescription = state.companion.label(),
            paused = state.isPaused,
        )
    }
}

@Composable
private fun InSessionPill(isPaused: Boolean, isFocus: Boolean) {
    val colors = PomtomTheme.colors
    val transition = rememberInfiniteTransition(label = "in-session-dot")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1100), repeatMode = RepeatMode.Reverse),
        label = "dot",
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (colors.isDark) colors.bg2.copy(alpha = 0.6f) else colors.surface2)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.4f), shape = RoundedCornerShape(percent = 50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(brush = Brush.linearGradient(listOf(colors.amber, colors.ember))),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = when {
                isPaused -> "PAUSED"
                isFocus -> "IN SESSION"
                else -> "BREAK"
            },
            style = PomtomTheme.typography.caption,
            color = colors.ink2,
        )
    }
    @Suppress("UNUSED_EXPRESSION") pulse
}

@Composable
private fun AmbientPeek(ambient: AmbientStatus, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val trailing = when {
        ambient.isPlaying -> "Playing"
        ambient.hasChoice -> "Ready"
        else -> "Choose"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softCard()
            .clickable(role = Role.Button, onClickLabel = "Open audio mixer", onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EqIcon(color = colors.amber, isPlaying = ambient.isPlaying, size = 20.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("AMBIENT", style = PomtomTheme.typography.caption, color = colors.ink3)
            Text(
                text = ambient.label,
                style = PomtomTheme.typography.label,
                color = colors.ink,
            )
        }
        Text(
            text = trailing,
            style = PomtomTheme.typography.label,
            color = colors.amber,
        )
    }
}

@Composable
private fun ActionCluster(
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(icon = Icons.Outlined.SkipNext, label = "Skip", onClick = onSkip)
        ActionButton(
            icon = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
            label = if (isPaused) "Resume" else "Pause",
            onClick = onPauseToggle,
            isPrimary = true,
        )
        ActionButton(icon = Icons.Outlined.Stop, label = "Stop", onClick = onStop)
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
) {
    val colors = PomtomTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (isPrimary) 64.dp else 52.dp)
                .clip(CircleShape)
                .background(
                    brush = if (isPrimary) Brush.linearGradient(listOf(colors.amber, colors.ember))
                    else Brush.linearGradient(listOf(colors.bg2, colors.bg2)),
                )
                .border(
                    width = if (isPrimary) 0.dp else 1.dp,
                    color = colors.ink3.copy(alpha = if (isPrimary) 0f else 0.4f),
                    shape = CircleShape,
                )
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) colors.onAccent else colors.ink2,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = PomtomTheme.typography.caption, color = colors.ink3)
    }
}

private fun SessionPhase.displayLabel(): String = when (this) {
    SessionPhase.FOCUS -> "FOCUS"
    SessionPhase.SHORT_BREAK -> "SHORT BREAK"
    SessionPhase.LONG_BREAK -> "LONG BREAK"
}

internal fun CompanionType.assetPath(): String? = when (this) {
    CompanionType.OFF -> null
    CompanionType.MEDITATING_FOX -> CompanionAssets.MEDITATING_FOX
    CompanionType.MOON_CAT -> CompanionAssets.MOON_CAT
    CompanionType.PLAYFUL_CAT -> CompanionAssets.PLAYFUL_CAT
}

internal fun CompanionType.label(): String = when (this) {
    CompanionType.OFF -> "No companion"
    CompanionType.MEDITATING_FOX -> "Meditating fox"
    CompanionType.MOON_CAT -> "Cat fishing on the moon"
    CompanionType.PLAYFUL_CAT -> "Playful cat"
}
