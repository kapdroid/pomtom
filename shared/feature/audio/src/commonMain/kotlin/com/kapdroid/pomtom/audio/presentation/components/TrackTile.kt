package com.kapdroid.pomtom.audio.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.audio.presentation.TrackRow
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.util.glassCard
import com.kapdroid.pomtom.domain.entity.AudioCategory
import com.kapdroid.pomtom.domain.entity.AudioSourceKind

@Composable
fun TrackTile(
    row: TrackRow,
    onToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onAddToFocus: () -> Unit,
    onMore: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val isPlaying = row.state.isPlaying
    val isFocus = row.isFocusAudio
    val highlight = isPlaying || isFocus
    val tileBackground by animateColorAsState(
        targetValue = if (highlight) colors.amber.copy(alpha = 0.10f) else colors.bg2.copy(alpha = 0.0f),
        label = "tile-bg",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCard()
            .background(color = tileBackground, shape = PomtomTheme.shapes.card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryAvatar(category = row.track.category)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.track.displayName,
                    style = PomtomTheme.typography.titleSans,
                    color = colors.ink,
                )
                Text(
                    text = row.subtitle(),
                    style = PomtomTheme.typography.caption,
                    color = if (isFocus) colors.amber else colors.ink3,
                )
            }
            PreviewPill(
                trackName = row.track.displayName,
                isPlaying = isPlaying,
                onClick = onToggle,
            )
            Spacer(Modifier.width(6.dp))
            AddButton(
                trackName = row.track.displayName,
                isAdded = isFocus,
                onClick = onAddToFocus,
            )
            if (onMore != null) {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onMore) {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "Track options", tint = colors.ink3)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        VolumeRow(
            trackName = row.track.displayName,
            volume = row.state.volume,
            isPlaying = isPlaying,
            onChange = onVolumeChange,
        )
        if (row.state.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = row.state.errorMessage!!,
                style = PomtomTheme.typography.caption,
                color = colors.ember,
            )
        }
    }
}

@Composable
private fun CategoryAvatar(category: AudioCategory) {
    val colors = PomtomTheme.colors
    val brush = when (category) {
        AudioCategory.AMBIENT -> Brush.linearGradient(listOf(colors.amber, colors.ember))
        AudioCategory.NATURE -> Brush.linearGradient(listOf(colors.sage, colors.violet))
        AudioCategory.INSTRUMENT -> Brush.linearGradient(listOf(colors.rose, colors.amber))
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(percent = 32))
            .background(brush = brush, shape = RoundedCornerShape(percent = 32)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.glyph(),
            style = PomtomTheme.typography.label,
            color = colors.onAccent,
        )
    }
}

@Composable
private fun PreviewPill(trackName: String, isPlaying: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val container by animateColorAsState(
        targetValue = if (isPlaying) colors.amber else colors.bg2.copy(alpha = 0.4f),
        label = "preview-pill-bg",
    )
    val content by animateColorAsState(
        targetValue = if (isPlaying) colors.onAccent else colors.ink,
        label = "preview-pill-fg",
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(color = container, shape = RoundedCornerShape(percent = 50))
            .clickable(
                role = Role.Button,
                onClickLabel = if (isPlaying) "Stop preview of $trackName" else "Preview $trackName",
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = content,
        )
    }
}

@Composable
private fun AddButton(trackName: String, isAdded: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val container by animateColorAsState(
        targetValue = if (isAdded) colors.amber else colors.bg2.copy(alpha = 0.4f),
        label = "add-btn-bg",
    )
    val tint by animateColorAsState(
        targetValue = if (isAdded) colors.onAccent else colors.ink2,
        label = "add-btn-fg",
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(color = container, shape = RoundedCornerShape(percent = 50))
            .clickable(
                role = Role.Button,
                onClickLabel = if (isAdded) {
                    "Remove $trackName from focus sessions"
                } else {
                    "Add $trackName to focus sessions"
                },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isAdded) Icons.Rounded.Check else Icons.Rounded.Add,
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
private fun VolumeRow(trackName: String, volume: Float, isPlaying: Boolean, onChange: (Float) -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "VOL",
            style = PomtomTheme.typography.caption,
            color = colors.ink3,
        )
        Slider(
            value = volume,
            onValueChange = onChange,
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .semantics { contentDescription = "$trackName volume" },
            colors = SliderDefaults.colors(
                thumbColor = if (isPlaying) colors.amber else colors.ink2,
                activeTrackColor = if (isPlaying) colors.amber else colors.ink3,
                inactiveTrackColor = colors.ink3.copy(alpha = 0.25f),
            ),
        )
        Text(
            text = "${(volume * 100).toInt()}",
            style = PomtomTheme.typography.mono,
            color = colors.ink2,
        )
    }
}

private fun TrackRow.subtitle(): String {
    val origin = when (track.source) {
        AudioSourceKind.BUNDLED -> "Bundled"
        AudioSourceKind.CUSTOM -> "Custom"
    }
    val mins = (track.durationMs / 60_000L).toInt()
    val durationLabel = if (mins > 0) "$mins min loop" else "Loop"
    val focusTag = if (isFocusAudio) " · Added" else ""
    return "$origin · $durationLabel$focusTag"
}

private fun AudioCategory.glyph(): String = when (this) {
    AudioCategory.AMBIENT -> "A"
    AudioCategory.NATURE -> "N"
    AudioCategory.INSTRUMENT -> "♪"
}
