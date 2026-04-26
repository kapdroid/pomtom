package com.kapdroid.pomtom.audio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.audio.presentation.components.CategoryHeader
import com.kapdroid.pomtom.audio.presentation.components.TrackTile
import com.kapdroid.pomtom.designsystem.components.AuroraBackground
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.domain.entity.AudioCategory
import com.kapdroid.pomtom.domain.entity.AudioSourceKind
import com.kapdroid.pomtom.filepicker.FileKind
import com.kapdroid.pomtom.filepicker.PickerCancelledException
import com.kapdroid.pomtom.filepicker.rememberFilePicker
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AudioMixerScreen(
    onClose: () -> Unit,
    viewModel: AudioMixerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberFilePicker(kind = FileKind.AUDIO) { result ->
        result.fold(
            onSuccess = { picked -> viewModel.onAudioPicked(displayName = picked.displayName, absolutePath = picked.absolutePath) },
            onFailure = { error ->
                if (error !== PickerCancelledException) viewModel.onPickerError(error.message)
            },
        )
    }

    // Previews are exploratory — never let one leak past navigation. The
    // configured focus track is unaffected (managed by FocusAudioController).
    DisposableEffect(viewModel) {
        onDispose { viewModel.stopAllPreviews() }
    }

    AuroraBackground {
        // Mixer is a tall scrolling list of track tiles — cap content width so on
        // tablets / landscape phones the rows don't stretch into a single ribbon.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 720.dp)
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
            ) {
                MixerHeader(
                    focusTrackName = state.focusTrackName,
                    previewingName = state.previewingName,
                    onClose = onClose,
                )
                Spacer(Modifier.height(12.dp))
                ImportButton(
                    isImporting = state.isImporting,
                    onClick = { launcher.launch() },
                )
                Spacer(Modifier.height(8.dp))
                ErrorStrip(
                    message = state.errorMessage,
                    onDismiss = { viewModel.onEvent(AudioMixerEvent.DismissError) },
                )
                TrackList(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MixerHeader(
    focusTrackName: String?,
    previewingName: String?,
    onClose: () -> Unit,
) {
    val colors = PomtomTheme.colors
    val subtitle = when {
        previewingName != null -> "Previewing · $previewingName"
        focusTrackName != null -> "$focusTrackName plays in focus"
        else -> "Tap + to set your focus sound"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Soundscape",
                style = PomtomTheme.typography.titleSerif,
                color = colors.ink,
            )
            Text(
                text = subtitle,
                style = PomtomTheme.typography.caption,
                color = if (focusTrackName != null && previewingName == null) colors.amber else colors.ink3,
            )
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Rounded.Close, contentDescription = "Close mixer", tint = colors.ink2)
        }
    }
}

@Composable
private fun ImportButton(isImporting: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PomtomTheme.shapes.card)
            .background(
                brush = Brush.horizontalGradient(listOf(colors.bg2, colors.bg1)),
                shape = PomtomTheme.shapes.card,
            )
            .clickable(enabled = !isImporting, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(percent = 32))
                .background(
                    brush = Brush.linearGradient(listOf(colors.violet, colors.amber)),
                    shape = RoundedCornerShape(percent = 32),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    color = colors.onAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.onAccent)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (isImporting) "Importing…" else "Add your own",
                style = PomtomTheme.typography.titleSans,
                color = colors.ink,
            )
            Text(
                text = "Pick any audio file. We'll loop it cleanly.",
                style = PomtomTheme.typography.caption,
                color = colors.ink3,
            )
        }
    }
}

@Composable
private fun ErrorStrip(message: String?, onDismiss: () -> Unit) {
    if (message.isNullOrBlank()) return
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PomtomTheme.shapes.chip)
            .background(color = colors.ember.copy(alpha = 0.18f), shape = PomtomTheme.shapes.chip)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = PomtomTheme.typography.caption,
            color = colors.ember,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = colors.ember)
        }
    }
}

@Composable
private fun TrackList(state: AudioMixerUiState, viewModel: AudioMixerViewModel) {
    val grouped = remember(state.tracks) {
        AudioMixerViewModel.groupByCategory(state.tracks)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AudioCategory.entries.forEach { category ->
            val rows = grouped[category] ?: return@forEach
            item(key = "header-$category") {
                Spacer(Modifier.height(4.dp))
                CategoryHeader(category = category, count = rows.size)
            }
            trackTiles(rows = rows, viewModel = viewModel)
        }
        item(key = "footer") { Spacer(Modifier.height(48.dp)) }
    }
}

private fun LazyListScope.trackTiles(
    rows: List<TrackRow>,
    viewModel: AudioMixerViewModel,
) {
    items(count = rows.size, key = { rows[it].track.id }) { index ->
        val row = rows[index]
        TrackTile(
            row = row,
            onToggle = { viewModel.onEvent(AudioMixerEvent.ToggleTrack(row.track.id)) },
            onVolumeChange = { gain -> viewModel.onEvent(AudioMixerEvent.SetVolume(row.track.id, gain)) },
            onAddToFocus = { viewModel.onEvent(AudioMixerEvent.ToggleFocusAudio(row.track.id)) },
            onMore = if (row.track.source == AudioSourceKind.CUSTOM) {
                { viewModel.onEvent(AudioMixerEvent.DeleteCustom(row.track.id)) }
            } else null,
        )
    }
}
