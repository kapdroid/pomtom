package com.kapdroid.pomtom.goals.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType

internal data class GoalGradient(val start: Color, val end: Color, val name: String)

@Composable
@ReadOnlyComposable
internal fun GoalColor.gradient(): GoalGradient {
    val colors = PomtomTheme.colors
    return when (this) {
        GoalColor.AMBER -> GoalGradient(colors.amber, colors.ember, "Sunset")
        GoalColor.EMBER -> GoalGradient(colors.ember, Color(0xFFB8421C), "Ember")
        GoalColor.ROSE -> GoalGradient(colors.rose, colors.ember, "Bloom")
        GoalColor.SAGE -> GoalGradient(colors.sage, Color(0xFF5D7A5A), "Forest")
        GoalColor.VIOLET -> GoalGradient(colors.violet, Color(0xFF6A54C4), "Dusk")
    }
}

internal fun GoalType.unitLabel(): String = when (this) {
    GoalType.SESSIONS -> "sessions"
    GoalType.MINUTES -> "minutes"
    GoalType.HOURS -> "hours"
    GoalType.DAYS -> "days"
}

internal fun GoalColor.glyph(): String = when (this) {
    GoalColor.AMBER -> "✱"
    GoalColor.EMBER -> "▲"
    GoalColor.ROSE -> "♡"
    GoalColor.SAGE -> "●"
    GoalColor.VIOLET -> "◆"
}
