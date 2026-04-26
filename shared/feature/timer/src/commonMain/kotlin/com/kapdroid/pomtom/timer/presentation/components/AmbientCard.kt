package com.kapdroid.pomtom.timer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.kapdroid.pomtom.designsystem.components.EqIcon
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.util.softCard
import androidx.compose.ui.unit.dp

@Composable
fun AmbientCard(
    label: String,
    isPlaying: Boolean,
    hasChoice: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val trailing = when {
        isPlaying -> "Playing"
        hasChoice -> "Ready"
        else -> "Choose"
    }
    val trailingColor = if (isPlaying || hasChoice) colors.amber else colors.ink2
    val labelColor = if (hasChoice || isPlaying) colors.ink else colors.ink2
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softCard()
            .clickable(role = Role.Button, onClickLabel = "Open ambient mixer", onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EqIcon(color = colors.amber, isPlaying = isPlaying, size = 22.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(text = "AMBIENT", style = PomtomTheme.typography.caption, color = colors.ink3)
            Spacer(Modifier.height(2.dp))
            Text(text = label, style = PomtomTheme.typography.titleSans, color = labelColor)
        }
        Text(
            text = trailing,
            style = PomtomTheme.typography.label,
            color = trailingColor,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.ink3,
        )
    }
}
