package com.kapdroid.pomtom.timer.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.kapdroid.pomtom.designsystem.util.stableSafeAreaPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.components.AuroraBackground
import com.kapdroid.pomtom.designsystem.components.BackInterceptor
import com.kapdroid.pomtom.designsystem.components.FocusCompanion
import com.kapdroid.pomtom.designsystem.components.ProgressRing
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.util.WithWindowMetrics
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.platform.FocusModeController
import com.kapdroid.pomtom.timer.presentation.util.TimeFormat
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val HOLD_DURATION_MS = 3_000

@Composable
fun StrictScreen(
    onExit: () -> Unit,
    onCelebrate: (sessionId: String, goalId: String?) -> Unit,
    viewModel: SessionViewModel = koinViewModel(),
    focusModeController: FocusModeController = koinInject(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nav by viewModel.navigationEvents.collectAsStateWithLifecycle()

    // Lock the user in. While the session is running or paused, the system back
    // gesture is intercepted and discarded — the only escape is the in-screen
    // 3-second hold-to-emergency-exit gesture (which routes through onAbort →
    // SessionUiEvent.Stop → navigation back to home).
    BackInterceptor(enabled = state.isRunning || state.isPaused)

    DisposableEffect(focusModeController) {
        focusModeController.enable()
        onDispose { focusModeController.disable() }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val current = viewModel.uiState.value
                if (current.isRunning || current.isPaused) {
                    viewModel.onEvent(SessionUiEvent.Stop)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Single navigation guard. Emergency exit fires both `SessionNavigation.Back` AND
    // makes the active session null — without this guard, both LaunchedEffects below
    // would call onExit/onCelebrate, leading to a double pop and the user landing on
    // the wrong screen (or empty back stack).
    var hasNavigatedAway by remember { mutableStateOf(false) }

    LaunchedEffect(nav) {
        when (val target = nav) {
            SessionNavigation.Back -> {
                if (!hasNavigatedAway) {
                    hasNavigatedAway = true
                    onExit()
                }
                viewModel.consumeNavigation()
            }
            is SessionNavigation.Celebrate -> {
                if (!hasNavigatedAway) {
                    hasNavigatedAway = true
                    onCelebrate(target.sessionId, target.goalId)
                }
                viewModel.consumeNavigation()
            }
            null -> Unit
        }
    }
    LaunchedEffect(state.session, state.isLoading) {
        if (!state.isLoading && state.session == null && !hasNavigatedAway) {
            hasNavigatedAway = true
            onExit()
        }
    }

    StrictScreenContent(
        state = state,
        onAbort = { viewModel.onEvent(SessionUiEvent.Stop) },
    )
}

@Composable
private fun StrictScreenContent(
    state: SessionUiState,
    onAbort: () -> Unit,
) {
    AuroraBackground {
        WithWindowMetrics { metrics ->
            if (metrics.isLandscape) StrictLandscape(state, onAbort)
            else StrictPortrait(state, onAbort)
        }
    }
}

@Composable
private fun StrictPortrait(state: SessionUiState, onAbort: () -> Unit) {
    // stableSafeAreaPadding (designsystem util, expect/actual): locks to a constant
    // safe-area inset that doesn't shrink when FocusModeController hides the system
    // bars. Solves two bugs at once on Android — the badge no longer slides up after
    // entry, and the cutout no longer clips it. iOS gets safeDrawingPadding() since
    // its FocusModeController doesn't hide the status bar in the first place.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(stableSafeAreaPadding())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        StrictBadge()
        Spacer(Modifier.weight(1f))
        StrictRing(state = state)
        Spacer(Modifier.height(28.dp))
        StrictQuote()
        StrictCompanionSlot(state = state, topPadding = 18.dp)
        Spacer(Modifier.weight(1f))
        StatusChips()
        Spacer(Modifier.height(14.dp))
        HoldToExitButton(onAbort = onAbort)
        Spacer(Modifier.height(8.dp))
    }
}

// Landscape strict layout: ring on the left half, supporting elements (badge, quote,
// companion, status chips, hold-to-exit) stacked on the right half. The strict mode's
// "stay-with-us" feel is preserved — the ring still anchors the eye, the hold-to-exit
// stays one-tap-away on the right edge.
@Composable
private fun StrictLandscape(state: SessionUiState, onAbort: () -> Unit) {
    // Same stable safe area as portrait — handles cutouts on side edges (landscape
    // notch position depends on rotation) and stays put through any system-bar
    // visibility change.
    Row(
        modifier = Modifier
            .fillMaxSize()
            .then(stableSafeAreaPadding())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            StrictRing(state = state)
        }
        Spacer(Modifier.width(20.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxSize().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StrictBadge()
            Spacer(Modifier.weight(1f))
            StrictQuote()
            StrictCompanionSlot(state = state, topPadding = 0.dp)
            Spacer(Modifier.weight(1f))
            StatusChips()
            HoldToExitButton(onAbort = onAbort)
        }
    }
}

/**
 * Reserves a fixed-height slot for the focus companion so the surrounding column
 * layout never reflows when the Lottie pops in.
 *
 * The shift this fixes: on first composition `state.session` is null while the timer
 * loads, so the original `if (asset != null && phase == FOCUS) { ... }` rendered
 * nothing. The two `Spacer(weight = 1f)` in the parent column then split a much
 * larger vertical space — the badge-to-ring gap was wide. A few hundred ms later the
 * session loaded, the companion appeared with its ~220 dp height, the weighted
 * spacers shrank, and the badge-to-ring gap visibly collapsed. By reserving the
 * companion's slot from the first frame (only when the user has a companion enabled),
 * the weighted spacers see the same available space throughout — no shift.
 *
 * If the user has set companion = OFF, no slot is reserved (the original gapless
 * behavior). For BREAK phases the slot stays empty but reserved, which is the right
 * call: the layout shouldn't dance every time a phase changes.
 */
@Composable
private fun StrictCompanionSlot(state: SessionUiState, topPadding: androidx.compose.ui.unit.Dp) {
    val asset = state.companion.assetPath() ?: return
    val isFocus = state.session?.phase == SessionPhase.FOCUS

    if (topPadding > 0.dp) Spacer(Modifier.height(topPadding))
    Box(
        // 220 dp matches FocusCompanion's default `sizeDp`. Pinning the slot to that
        // height holds the column layout stable whether the Lottie is currently
        // rendering or the slot is briefly empty (between phases / during load).
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isFocus) {
            FocusCompanion(
                assetPath = asset,
                contentDescription = state.companion.label(),
                paused = state.isPaused,
            )
        }
    }
}

@Composable
private fun StrictBadge() {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.amber.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = colors.amber.copy(alpha = 0.4f),
                shape = RoundedCornerShape(percent = 50),
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .semantics { contentDescription = "Strict mode active" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = colors.amber,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = "STRICT MODE · ACTIVE",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 10.sp,
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.amber,
        )
    }
}

@Composable
private fun StrictRing(state: SessionUiState) {
    val colors = PomtomTheme.colors
    ProgressRing(
        progress = state.progress,
        isRunning = state.isRunning,
        diameter = 300.dp,
    ) {
        // Top spacer (~24dp) balances the goal-title block below the time vertically,
        // so the time lands at the column's geometric center. fillMaxWidth +
        // CenterHorizontally then ensures each child centers on the ring's true
        // horizontal axis (otherwise the column collapses to the widest child and the
        // narrower time text drifts off the ring's centerline).
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = TimeFormat.mmss(state.remainingMs),
                style = PomtomTheme.typography.timer.copy(
                    fontSize = 76.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = colors.ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.attachedGoal?.title ?: "Pure focus",
                style = PomtomTheme.typography.titleSerif.copy(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = colors.ink2,
            )
        }
    }
}

@Composable
private fun StrictQuote() {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier.widthIn(max = 280.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\u201CThe best way out is always through.\u201D",
            style = PomtomTheme.typography.titleSerif.copy(
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp,
            ),
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "\u2014 ROBERT FROST",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 9.sp,
                letterSpacing = 2.7.sp,
            ),
            color = colors.ink3,
        )
    }
}

@Composable
private fun StatusChips() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusChip("WAKE-LOCK")
        StatusChip("FULLSCREEN")
        StatusChip("LOGGED")
    }
}

@Composable
private fun StatusChip(label: String) {
    val colors = PomtomTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(colors.sage),
        )
        Text(
            text = label,
            style = PomtomTheme.typography.mono.copy(
                fontSize = 8.sp,
                letterSpacing = 2.0.sp,
            ),
            color = colors.ink3,
        )
    }
}

@Composable
private fun HoldToExitButton(onAbort: () -> Unit) {
    val colors = PomtomTheme.colors
    var holding by remember { mutableStateOf(false) }
    val holdProgress = remember { Animatable(0f) }

    LaunchedEffect(holding) {
        if (holding) {
            holdProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = HOLD_DURATION_MS, easing = LinearEasing),
            )
            if (holding) onAbort()
        } else {
            holdProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = LinearEasing),
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 320.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = colors.ink3.copy(alpha = 0.45f),
                shape = RoundedCornerShape(percent = 50),
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        tryAwaitRelease()
                        holding = false
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val width = maxWidth
        Box(
            modifier = Modifier
                .width(width * holdProgress.value)
                .fillMaxSize()
                .align(Alignment.CenterStart)
                .background(brush = Brush.horizontalGradient(listOf(colors.ember, colors.amber))),
        )
        val secondsLeft = ((HOLD_DURATION_MS / 1000f) * (1f - holdProgress.value))
            .coerceAtLeast(0f)
        Text(
            text = if (holding) "Release to cancel \u00B7 ${formatSeconds(secondsLeft)}s"
            else "Hold to emergency exit",
            style = PomtomTheme.typography.label,
            color = if (holdProgress.value > 0.55f) colors.onAccent else colors.ink2,
        )
    }
}

private fun formatSeconds(value: Float): String {
    val rounded = (value * 10f).roundToInt() / 10f
    val whole = rounded.toInt()
    val tenth = ((rounded - whole) * 10f).roundToInt()
    return "$whole.$tenth"
}
