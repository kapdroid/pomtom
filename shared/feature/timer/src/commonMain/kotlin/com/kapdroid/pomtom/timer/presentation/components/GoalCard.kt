package com.kapdroid.pomtom.timer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.util.softCard
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType

@Composable
fun GoalCard(goal: Goal, modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    val accent = goal.color.toAccent(colors)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softCard()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush = Brush.linearGradient(listOf(accent.first, accent.second))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = goal.title.firstLetter(),
                style = PomtomTheme.typography.titleSerif.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified),
                color = colors.onAccent,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "WORKING TOWARD",
                style = PomtomTheme.typography.caption,
                color = colors.ink3,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = goal.title,
                style = PomtomTheme.typography.titleSans,
                color = colors.ink,
            )
            Spacer(Modifier.height(10.dp))
            ProgressBar(progress = goal.percent, accent = accent)
        }
        Spacer(Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${goal.progress}/${goal.target}",
                style = PomtomTheme.typography.mono,
                color = colors.ink2,
            )
            Spacer(Modifier.height(2.dp))
            Text(text = goal.type.displayName(), style = PomtomTheme.typography.caption, color = colors.ink3)
        }
    }
}

@Composable
fun GoalSuggestionCard(modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softCard()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("NO ACTIVE GOAL", style = PomtomTheme.typography.caption, color = colors.ink3)
            Spacer(Modifier.height(2.dp))
            Text("Name what you're working toward", style = PomtomTheme.typography.titleSans, color = colors.ink)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(brush = Brush.linearGradient(listOf(colors.amber, colors.ember))),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", style = PomtomTheme.typography.titleSerif, color = colors.onAccent)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, accent: Pair<Color, Color>) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.ink3.copy(alpha = 0.18f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(brush = Brush.horizontalGradient(listOf(accent.first, accent.second))),
        )
    }
}

private fun GoalType.displayName(): String = when (this) {
    GoalType.SESSIONS -> "SESSIONS"
    GoalType.MINUTES -> "MIN"
    GoalType.HOURS -> "HRS"
    GoalType.DAYS -> "DAYS"
}

private fun String.firstLetter(): String =
    firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "•"

private fun GoalColor.toAccent(colors: com.kapdroid.pomtom.designsystem.theme.PomtomColors): Pair<Color, Color> = when (this) {
    GoalColor.AMBER -> colors.amber to colors.ember
    GoalColor.EMBER -> colors.ember to colors.amber
    GoalColor.ROSE -> colors.rose to colors.ember
    GoalColor.SAGE -> colors.sage to colors.amber
    GoalColor.VIOLET -> colors.violet to colors.amber
}
