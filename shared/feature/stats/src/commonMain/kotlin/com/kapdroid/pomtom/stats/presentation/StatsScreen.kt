package com.kapdroid.pomtom.stats.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.components.AuroraBackground
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.domain.entity.SessionStatus
import com.kapdroid.pomtom.domain.repository.DailyFocus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.max

@Composable
fun StatsScreen(
    onOpenHistory: () -> Unit,
    viewModel: StatsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuroraBackground {
        StatsContent(state = state, onOpenHistory = onOpenHistory)
    }
}

@Composable
private fun StatsContent(state: StatsUiState, onOpenHistory: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { Header() }
        item { HeroCard(state = state) }
        item { WeeklyChartCard(byDay = state.byDay) }
        item { MetricsRow(state = state) }
        item { RecentSessionsCard(items = state.recentSessions, onOpenHistory = onOpenHistory) }
    }
}

@Composable
private fun Header() {
    val colors = PomtomTheme.colors
    Column {
        Text(
            text = "STATS · LAST 7 DAYS",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 10.sp,
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.ink3,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Your week",
            style = PomtomTheme.typography.titleSerif.copy(
                fontSize = 32.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = colors.ink,
        )
    }
}

@Composable
private fun HeroCard(state: StatsUiState) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(colors.ink, colors.bg3)))
            .padding(22.dp),
    ) {
        Text(
            text = "Focused this week",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 10.sp,
                letterSpacing = 2.0.sp,
            ),
            color = colors.ink3,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = formatDuration(state.totalFocusedMs),
            style = PomtomTheme.typography.display.copy(
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = colors.surface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${state.sessionCount} session${if (state.sessionCount == 1) "" else "s"} \u00B7 ${state.streakDays}-day streak",
            style = PomtomTheme.typography.body.copy(fontSize = 13.sp),
            color = colors.ink3,
        )
    }
}

@Composable
private fun WeeklyChartCard(byDay: List<DailyFocus>) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bg2.copy(alpha = 0.55f))
            .border(1.dp, colors.ink3.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Text(
            text = "DAILY FOCUS",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 10.sp,
                letterSpacing = 2.0.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.ink3,
        )
        Spacer(Modifier.height(14.dp))
        WeeklyBars(byDay = byDay)
    }
}

@Composable
private fun WeeklyBars(byDay: List<DailyFocus>) {
    val colors = PomtomTheme.colors
    val maxMs = max(1L, byDay.maxOfOrNull { it.focusedMs } ?: 0L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        byDay.forEach { day ->
            val ratio = day.focusedMs.toFloat() / maxMs.toFloat()
            val dayLabel = day.date.dayOfWeek.shortLabel()
            val minutes = (day.focusedMs / 60_000L).toInt()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "$dayLabel: $minutes focused minutes" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                ) {
                    val barWidth = size.width
                    val barHeight = size.height * ratio.coerceAtLeast(0.04f)
                    val cornerRadius = 8.dp.toPx()
                    drawRoundRect(
                        brush = if (day.focusedMs > 0) Brush.verticalGradient(listOf(colors.amber, colors.ember))
                        else Brush.verticalGradient(listOf(colors.ink3.copy(alpha = 0.18f), colors.ink3.copy(alpha = 0.10f))),
                        topLeft = Offset(0f, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = dayLabel,
                    style = PomtomTheme.typography.mono.copy(fontSize = 9.sp, letterSpacing = 1.4.sp),
                    color = colors.ink3,
                )
            }
        }
    }
}

@Composable
private fun MetricsRow(state: StatsUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricTile(
            label = "Streak",
            value = "${state.streakDays}d",
            icon = Icons.Outlined.LocalFireDepartment,
            modifier = Modifier.weight(1f),
        )
        MetricTile(
            label = "Sessions",
            value = "${state.sessionCount}",
            icon = Icons.Outlined.Timer,
            modifier = Modifier.weight(1f),
        )
        MetricTile(
            label = "Goals",
            value = "${(state.goalCompletionRate * 100).toInt()}%",
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bg2.copy(alpha = 0.55f))
            .border(1.dp, colors.ink3.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.amber,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            style = PomtomTheme.typography.titleSerif.copy(
                fontSize = 22.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.ink,
        )
        Text(
            text = label.uppercase(),
            style = PomtomTheme.typography.mono.copy(fontSize = 9.sp, letterSpacing = 1.6.sp),
            color = colors.ink3,
        )
    }
}

@Composable
private fun RecentSessionsCard(
    items: List<RecentSessionItem>,
    onOpenHistory: () -> Unit,
) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bg2.copy(alpha = 0.55f))
            .border(1.dp, colors.ink3.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "RECENT",
                style = PomtomTheme.typography.mono.copy(
                    fontSize = 10.sp,
                    letterSpacing = 2.0.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink3,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .clickable(role = Role.Button, onClick = onOpenHistory)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics { contentDescription = "Open history" },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "View all",
                    style = PomtomTheme.typography.mono.copy(fontSize = 11.sp, letterSpacing = 1.0.sp),
                    color = colors.amber,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.amber,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        if (items.isEmpty()) {
            EmptyRecent()
        } else {
            items.forEachIndexed { index, item ->
                if (index > 0) RowDivider()
                RecentRow(item = item)
            }
        }
    }
}

@Composable
private fun RowDivider() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(1.dp)
            .background(colors.ink3.copy(alpha = 0.12f)),
    )
}

@Composable
private fun RecentRow(item: RecentSessionItem) {
    val colors = PomtomTheme.colors
    val session = item.session
    val timeText = formatHourMinute(session.startedAtMs)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.goalTitle ?: "Focus block",
                style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                color = colors.ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = timeText,
                style = PomtomTheme.typography.mono.copy(fontSize = 10.sp, letterSpacing = 1.0.sp),
                color = colors.ink3,
            )
        }
        Spacer(Modifier.width(10.dp))
        StatusChip(status = session.status)
        Spacer(Modifier.width(10.dp))
        Text(
            text = formatDuration(session.actualMs),
            style = PomtomTheme.typography.mono.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            color = colors.ink,
        )
    }
}

@Composable
internal fun StatusChip(status: SessionStatus) {
    val colors = PomtomTheme.colors
    val (label, tint) = when (status) {
        SessionStatus.COMPLETED -> "DONE" to colors.amber
        SessionStatus.ABORTED -> "BROKEN" to colors.ember
        SessionStatus.RUNNING -> "ACTIVE" to colors.sage
        SessionStatus.PAUSED -> "PAUSED" to colors.violet
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(tint.copy(alpha = 0.18f))
            .border(1.dp, tint.copy(alpha = 0.50f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = PomtomTheme.typography.mono.copy(
                fontSize = 9.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = tint,
        )
    }
}

@Composable
private fun EmptyRecent() {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No sessions yet",
            style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = colors.ink2,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Begin a focus block to start your streak.",
            style = PomtomTheme.typography.body.copy(fontSize = 12.sp),
            color = colors.ink3,
        )
    }
}

internal fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalMinutes = ms / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
        hours > 0L -> "${hours}h"
        else -> "${minutes}m"
    }
}

internal fun formatHourMinute(epochMs: Long): String {
    val zone = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(zone)
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "M"
    DayOfWeek.TUESDAY -> "T"
    DayOfWeek.WEDNESDAY -> "W"
    DayOfWeek.THURSDAY -> "T"
    DayOfWeek.FRIDAY -> "F"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "S"
}
