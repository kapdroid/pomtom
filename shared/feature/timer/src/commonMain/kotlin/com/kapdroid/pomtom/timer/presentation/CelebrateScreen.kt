package com.kapdroid.pomtom.timer.presentation

import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.components.AuroraBackground
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.timer.presentation.components.ConfettiOverlay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CelebrateScreen(
    sessionId: String,
    goalId: String?,
    onClose: () -> Unit,
    onSetNextGoal: () -> Unit,
    viewModel: CelebrateViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId, goalId) {
        viewModel.onEvent(CelebrateUiEvent.Bind(sessionId = sessionId, goalId = goalId))
    }
    AuroraBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            ConfettiOverlay(pieceCount = 56, seed = sessionId.hashCode().toLong())
            CelebrateContent(
                state = state,
                onClose = onClose,
                onSetNextGoal = onSetNextGoal,
            )
        }
    }
}

@Composable
private fun CelebrateContent(
    state: CelebrateUiState,
    onClose: () -> Unit,
    onSetNextGoal: () -> Unit,
) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HaloMark()
            Spacer(Modifier.height(22.dp))
            Text(
                text = if (state.goal != null) "GOAL COMPLETE" else "SESSION COMPLETE",
                style = PomtomTheme.typography.caption.copy(letterSpacing = 3.0.sp),
                color = colors.amber,
            )
            Spacer(Modifier.height(12.dp))
            CelebrateHeadline(state = state)
            Spacer(Modifier.height(14.dp))
            CelebrateSubcopy(state = state)
            Spacer(Modifier.height(28.dp))
            CelebrateStatsRow(state = state)
        }
        Spacer(modifier = Modifier.weight(1f))
        CelebrateActions(
            hasGoal = state.goal != null,
            onClose = onClose,
            onSetNextGoal = onSetNextGoal,
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun HaloMark() {
    val colors = PomtomTheme.colors
    val transition = rememberInfiniteTransition(label = "halo")
    val breathe by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo-breathe",
    )
    Box(
        modifier = Modifier.size(150.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(breathe)
                .size(150.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.amber.copy(alpha = 0.40f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = Brush.linearGradient(listOf(colors.amber, colors.ember)),
                    shape = androidx.compose.foundation.shape.CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CheckMark(color = colors.bg0)
        }
    }
}

@Composable
private fun CheckMark(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(64.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round, pathEffect = PathEffect.cornerPathEffect(8f))
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.74f)
            lineTo(w * 0.78f, h * 0.30f)
        }
        drawPath(path = path, color = color, style = stroke)
    }
}

@Composable
private fun CelebrateHeadline(state: CelebrateUiState) {
    val colors = PomtomTheme.colors
    val title = state.goal?.title ?: "A finished session."
    val parts = if (state.goal != null) splitTitle(title) else null
    if (parts == null) {
        Text(
            text = title,
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
    } else {
        Text(
            text = parts.first,
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
        Text(
            text = parts.second,
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
    }
}

private fun splitTitle(title: String): Pair<String, String> {
    val trimmed = title.trim()
    val splitIndex = trimmed.lastIndexOfAny(charArrayOf(' ', '·', '-', '—'))
    if (splitIndex <= 0) return Pair(trimmed, "")
    val tail = trimmed.substring(splitIndex + 1).trim()
    val head = trimmed.substring(0, splitIndex).trim()
    if (tail.isEmpty()) return Pair(trimmed, "")
    return Pair("$head ", tail + ".")
}

@Composable
private fun CelebrateSubcopy(state: CelebrateUiState) {
    val colors = PomtomTheme.colors
    val sub = buildSubcopy(state)
    Text(
        text = sub,
        style = PomtomTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
        color = colors.ink2,
        modifier = Modifier.alpha(0.95f),
    )
}

private fun buildSubcopy(state: CelebrateUiState): String {
    val focused = humanDuration(state.focusedMs)
    return when {
        state.goal != null && state.sessionsCount > 0 -> "${state.sessionsCount} sessions. $focused of quiet work. That's a draft."
        state.goal != null -> "Goal complete. Take a breath."
        state.sessionFocusedMs > 0 -> "${humanDuration(state.sessionFocusedMs)} of focus. Well done."
        else -> "Quiet work, finished."
    }
}

private fun humanDuration(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalMinutes = (ms / 60_000L).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

@Composable
private fun CelebrateStatsRow(state: CelebrateUiState) {
    val sessions = if (state.goal != null) state.sessionsCount else 1
    val focusedLabel = if (state.goal != null && state.focusedMs > 0L) humanDuration(state.focusedMs)
    else humanDuration(state.sessionFocusedMs)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MiniStat(value = sessions.toString(), label = "SESSIONS")
        Divider()
        MiniStat(value = focusedLabel, label = "FOCUSED")
        Divider()
        MiniStat(value = state.brokenCount.toString(), label = "BROKEN")
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    val colors = PomtomTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = PomtomTheme.typography.titleSerif.copy(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 26.sp,
                lineHeight = 30.sp,
            ),
            color = colors.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = PomtomTheme.typography.caption.copy(letterSpacing = 2.4.sp, fontSize = 9.sp),
            color = colors.ink3,
        )
    }
}

@Composable
private fun Divider() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(color = colors.ink3.copy(alpha = 0.30f)),
    )
}

@Composable
private fun CelebrateActions(
    hasGoal: Boolean,
    onClose: () -> Unit,
    onSetNextGoal: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryButton(
            label = "Close",
            onClick = onClose,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            label = if (hasGoal) "Set next goal" else "Back to focus",
            onClick = onSetNextGoal,
            modifier = Modifier.weight(2f),
        )
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(listOf(colors.amber, colors.ember)),
                shape = shape,
            )
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PomtomTheme.typography.bodyEmphasis.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            color = colors.onAccent,
        )
    }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(color = colors.ink.copy(alpha = 0.08f), shape = shape)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.30f), shape = shape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PomtomTheme.typography.bodyEmphasis.copy(fontSize = 13.sp),
            color = colors.ink,
        )
    }
}
