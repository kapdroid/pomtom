package com.kapdroid.pomtom.timer.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import kotlin.time.Duration

@Composable
fun DurationPicker(
    options: List<Duration>,
    selected: Duration,
    onSelected: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val pillShape = RoundedCornerShape(percent = 50)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(pillShape)
            .background(if (colors.isDark) colors.bg2.copy(alpha = 0.65f) else colors.surface2)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.30f), shape = pillShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { duration ->
            val isSelected = duration == selected
            val height by animateDpAsState(targetValue = if (isSelected) 46.dp else 38.dp, label = "dp-h")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(pillShape)
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(colors.amber, colors.ember))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                    )
                    .clickable(role = Role.RadioButton) { onSelected(duration) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${duration.inWholeMinutes} min",
                    style = PomtomTheme.typography.label,
                    color = if (isSelected) colors.onAccent else colors.ink2,
                )
            }
        }
    }
}
