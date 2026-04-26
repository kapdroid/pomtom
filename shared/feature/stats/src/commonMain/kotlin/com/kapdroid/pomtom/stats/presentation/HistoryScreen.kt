package com.kapdroid.pomtom.stats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kapdroid.pomtom.domain.entity.SessionPhase
import com.kapdroid.pomtom.domain.entity.SessionStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AuroraBackground {
        HistoryContent(state = state, onBack = onBack)
    }
}

@Composable
private fun HistoryContent(state: HistoryUiState, onBack: () -> Unit) {
    // Same pattern as Stats: centered max-720dp content rail.
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .statusBarsPadding(),
        ) {
            HistoryHeader(onBack = onBack)
            if (state.groups.isEmpty() && !state.isLoading) {
                EmptyHistory()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    state.groups.forEach { group ->
                        item(key = "header-${group.date}") { DayHeader(date = group.date) }
                        item(key = "card-${group.date}") { DayCard(group = group) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(onBack: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.bg2.copy(alpha = 0.55f))
                .border(1.dp, colors.ink3.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = "Back" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = "HISTORY",
                style = PomtomTheme.typography.mono.copy(
                    fontSize = 10.sp,
                    letterSpacing = 2.4.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink3,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Every block, kept",
                style = PomtomTheme.typography.titleSerif.copy(
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = colors.ink,
            )
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate) {
    val colors = PomtomTheme.colors
    Text(
        text = formatDayHeader(date),
        style = PomtomTheme.typography.mono.copy(
            fontSize = 10.sp,
            letterSpacing = 2.0.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        color = colors.ink3,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun DayCard(group: HistoryDayGroup) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bg2.copy(alpha = 0.55f))
            .border(1.dp, colors.ink3.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp),
    ) {
        group.entries.forEachIndexed { index, entry ->
            if (index > 0) EntryDivider()
            HistoryRow(entry = entry)
        }
    }
}

@Composable
private fun EntryDivider() {
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
private fun HistoryRow(entry: HistoryEntry) {
    val colors = PomtomTheme.colors
    val session = entry.session
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PhaseDot(phase = session.phase)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.goalTitle ?: session.phase.label(),
                style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                color = colors.ink,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatHourMinute(session.startedAtMs),
                    style = PomtomTheme.typography.mono.copy(fontSize = 10.sp, letterSpacing = 1.0.sp),
                    color = colors.ink3,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "\u00B7",
                    style = PomtomTheme.typography.mono.copy(fontSize = 10.sp),
                    color = colors.ink3,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = session.phase.label(),
                    style = PomtomTheme.typography.mono.copy(fontSize = 10.sp, letterSpacing = 1.0.sp),
                    color = colors.ink3,
                )
                if (session.strictMode) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "\u00B7  STRICT",
                        style = PomtomTheme.typography.mono.copy(fontSize = 10.sp, letterSpacing = 1.0.sp),
                        color = colors.ember,
                    )
                }
            }
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
private fun PhaseDot(phase: SessionPhase) {
    val colors = PomtomTheme.colors
    val color = when (phase) {
        SessionPhase.FOCUS -> colors.amber
        SessionPhase.SHORT_BREAK -> colors.sage
        SessionPhase.LONG_BREAK -> colors.violet
    }
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(color),
    )
}

@Composable
private fun EmptyHistory() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nothing yet",
                style = PomtomTheme.typography.titleSerif.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
                color = colors.ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Finished sessions land here \u2014 streaks and broken blocks both.",
                style = PomtomTheme.typography.body.copy(fontSize = 13.sp),
                color = colors.ink3,
            )
        }
    }
}

private fun SessionPhase.label(): String = when (this) {
    SessionPhase.FOCUS -> "FOCUS"
    SessionPhase.SHORT_BREAK -> "SHORT BREAK"
    SessionPhase.LONG_BREAK -> "LONG BREAK"
}

private fun formatDayHeader(date: LocalDate): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (date) {
        today -> "TODAY"
        today.minusOne() -> "YESTERDAY"
        else -> date.uppercaseLabel()
    }
}

private fun LocalDate.minusOne(): LocalDate = LocalDate.fromEpochDays(toEpochDays() - 1)

private fun LocalDate.uppercaseLabel(): String =
    "${dayOfWeek.shortName()} \u00B7 ${month.shortName()} ${dayOfMonth.toString().padStart(2, '0')}"

private fun kotlinx.datetime.DayOfWeek.shortName(): String = when (this) {
    kotlinx.datetime.DayOfWeek.MONDAY -> "MON"
    kotlinx.datetime.DayOfWeek.TUESDAY -> "TUE"
    kotlinx.datetime.DayOfWeek.WEDNESDAY -> "WED"
    kotlinx.datetime.DayOfWeek.THURSDAY -> "THU"
    kotlinx.datetime.DayOfWeek.FRIDAY -> "FRI"
    kotlinx.datetime.DayOfWeek.SATURDAY -> "SAT"
    kotlinx.datetime.DayOfWeek.SUNDAY -> "SUN"
}

private fun kotlinx.datetime.Month.shortName(): String = when (this) {
    kotlinx.datetime.Month.JANUARY -> "JAN"
    kotlinx.datetime.Month.FEBRUARY -> "FEB"
    kotlinx.datetime.Month.MARCH -> "MAR"
    kotlinx.datetime.Month.APRIL -> "APR"
    kotlinx.datetime.Month.MAY -> "MAY"
    kotlinx.datetime.Month.JUNE -> "JUN"
    kotlinx.datetime.Month.JULY -> "JUL"
    kotlinx.datetime.Month.AUGUST -> "AUG"
    kotlinx.datetime.Month.SEPTEMBER -> "SEP"
    kotlinx.datetime.Month.OCTOBER -> "OCT"
    kotlinx.datetime.Month.NOVEMBER -> "NOV"
    kotlinx.datetime.Month.DECEMBER -> "DEC"
}
