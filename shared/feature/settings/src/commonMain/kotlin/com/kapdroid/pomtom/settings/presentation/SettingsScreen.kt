package com.kapdroid.pomtom.settings.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kapdroid.pomtom.designsystem.components.CompanionAssets
import com.kapdroid.pomtom.designsystem.components.CompanionPreviewTile
import com.kapdroid.pomtom.designsystem.theme.LocalPomtomColors
import com.kapdroid.pomtom.designsystem.theme.PomtomColors
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.theme.palettes.DuskPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.ForestPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.PaperPalette
import com.kapdroid.pomtom.designsystem.theme.palettes.RosePalette
import com.kapdroid.pomtom.domain.entity.AppTheme
import com.kapdroid.pomtom.domain.entity.CompanionType
import com.kapdroid.pomtom.domain.entity.SessionConfig
import com.kapdroid.pomtom.domain.entity.Wallpaper
import com.kapdroid.pomtom.domain.entity.WallpaperSource
import com.kapdroid.pomtom.filepicker.FileKind
import com.kapdroid.pomtom.filepicker.PickerCancelledException
import com.kapdroid.pomtom.filepicker.rememberFilePicker
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberFilePicker(kind = FileKind.IMAGE) { result ->
        result.fold(
            onSuccess = { picked ->
                viewModel.onEvent(
                    SettingsUiEvent.ImportWallpaper(
                        sourceUri = picked.absolutePath,
                        displayName = picked.displayName,
                    ),
                )
            },
            onFailure = { error ->
                if (error !== PickerCancelledException) {
                    viewModel.onEvent(SettingsUiEvent.WallpaperPickerError(error.message))
                }
            },
        )
    }
    CompositionLocalProvider(LocalPomtomColors provides PaperPalette) {
        SettingsContent(
            state = state,
            onBack = onBack,
            onLaunchPicker = { launcher.launch() },
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onLaunchPicker: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg0),
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            SettingsHeader(onBack = onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item { ProfileCard() }
                item { SessionSection(state.sessionConfig, onEvent) }
                item { StrictSection(state.sessionConfig.strictMode, onEvent) }
                item { CompanionSection(state.companion, onEvent) }
                item { AestheticSection(state.theme, onEvent) }
                item {
                    WallpaperSection(
                        wallpapers = state.wallpapers,
                        activeId = state.activeWallpaperId,
                        isImporting = state.isImportingWallpaper,
                        errorMessage = state.wallpaperError,
                        onLaunchPicker = onLaunchPicker,
                        onEvent = onEvent,
                    )
                }
                item { Footer() }
            }
        }
    }

    state.activeEditor?.let { field ->
        DurationEditorSheet(
            field = field,
            sessionConfig = state.sessionConfig,
            onDismiss = { onEvent(SettingsUiEvent.DismissEditor) },
            onEvent = onEvent,
        )
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.ink.copy(alpha = 0.04f))
                .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.30f), shape = RoundedCornerShape(14.dp))
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
    }
}

@Composable
private fun ProfileCard() {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.ink)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(colors.amber, colors.ember))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "P",
                style = PomtomTheme.typography.titleSerif.copy(
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.bg0,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pomtom · focused",
                style = PomtomTheme.typography.titleSerif.copy(
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = colors.surface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "QUIET HOURS · DEEP WORK",
                style = PomtomTheme.typography.mono.copy(
                    fontSize = 9.sp,
                    letterSpacing = 2.0.sp,
                ),
                color = colors.ink3,
            )
        }
    }
}

@Composable
private fun SessionSection(config: SessionConfig, onEvent: (SettingsUiEvent) -> Unit) {
    Section(title = "Session") {
        DurationRow(
            label = "Focus",
            value = "${config.focus.inWholeMinutes} min",
            onClick = { onEvent(SettingsUiEvent.OpenEditor(DurationField.FOCUS)) },
        )
        Divider()
        DurationRow(
            label = "Short break",
            value = "${config.shortBreak.inWholeMinutes} min",
            onClick = { onEvent(SettingsUiEvent.OpenEditor(DurationField.SHORT_BREAK)) },
        )
        Divider()
        DurationRow(
            label = "Long break",
            value = "${config.longBreak.inWholeMinutes} min",
            onClick = { onEvent(SettingsUiEvent.OpenEditor(DurationField.LONG_BREAK)) },
        )
        Divider()
        DurationRow(
            label = "Long break every",
            value = "${config.cyclesBeforeLong} cycles",
            onClick = { onEvent(SettingsUiEvent.OpenEditor(DurationField.CYCLES_BEFORE_LONG)) },
        )
    }
}

@Composable
private fun StrictSection(enabled: Boolean, onEvent: (SettingsUiEvent) -> Unit) {
    Section(title = "Strict mode") {
        ToggleRow(
            label = "Enforce focus",
            description = "Fullscreen, wake-lock, 3-second hold to exit. Broken sessions logged.",
            checked = enabled,
            onCheckedChange = { onEvent(SettingsUiEvent.SetStrictMode(it)) },
        )
        Divider()
        StrictNote()
    }
}

@Composable
private fun CompanionSection(selected: CompanionType, onEvent: (SettingsUiEvent) -> Unit) {
    Section(title = "Focus companion") {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompanionType.entries.forEach { type ->
                CompanionRow(
                    type = type,
                    selected = type == selected,
                    onClick = { onEvent(SettingsUiEvent.SetCompanion(type)) },
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Soft animated creature shown only during focus phases. " +
                    "Pick None to hide it entirely.",
                style = PomtomTheme.typography.body.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = PomtomTheme.colors.ink3,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun CompanionRow(
    type: CompanionType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.amber.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.amber.copy(alpha = 0.6f) else colors.ink3.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = "${type.title()}, ${if (selected) "selected" else "not selected"}" }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanionPreviewTile(
            assetPath = type.assetPath(),
            widthDp = 76.dp,
            heightDp = 52.dp,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type.title(),
                style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                color = colors.ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = type.subtitle(),
                style = PomtomTheme.typography.body.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = colors.ink3,
            )
        }
        if (selected) {
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.amber,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun CompanionType.assetPath(): String? = when (this) {
    CompanionType.OFF -> null
    CompanionType.MEDITATING_FOX -> CompanionAssets.MEDITATING_FOX
    CompanionType.MOON_CAT -> CompanionAssets.MOON_CAT
    CompanionType.PLAYFUL_CAT -> CompanionAssets.PLAYFUL_CAT
}

private fun CompanionType.title(): String = when (this) {
    CompanionType.OFF -> "None"
    CompanionType.MEDITATING_FOX -> "Meditating fox"
    CompanionType.MOON_CAT -> "Moonlit cat"
    CompanionType.PLAYFUL_CAT -> "Playful cat"
}

private fun CompanionType.subtitle(): String = when (this) {
    CompanionType.OFF -> "No companion. Just the ring."
    CompanionType.MEDITATING_FOX -> "Calm lotus pose. Embodies focus."
    CompanionType.MOON_CAT -> "Cat fishing on the moon. Quiet whimsy."
    CompanionType.PLAYFUL_CAT -> "Light, bouncy energy for active sessions."
}

@Composable
private fun AestheticSection(selected: AppTheme, onEvent: (SettingsUiEvent) -> Unit) {
    Section(title = "Aesthetic") {
        ThemeGrid(
            selected = selected,
            onSelect = { onEvent(SettingsUiEvent.SetTheme(it)) },
        )
    }
}

@Composable
private fun WallpaperSection(
    wallpapers: List<Wallpaper>,
    activeId: String?,
    isImporting: Boolean,
    errorMessage: String?,
    onLaunchPicker: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val userWallpapers = wallpapers.filter { it.source == WallpaperSource.USER }
    Section(title = "Wallpaper") {
        Column(modifier = Modifier.padding(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    NoneTile(
                        selected = activeId == null,
                        onClick = { onEvent(SettingsUiEvent.SelectWallpaper(null)) },
                    )
                }
                items(userWallpapers, key = { it.id }) { wallpaper ->
                    WallpaperTile(
                        wallpaper = wallpaper,
                        selected = wallpaper.id == activeId,
                        onSelect = { onEvent(SettingsUiEvent.SelectWallpaper(wallpaper.id)) },
                        onDelete = { onEvent(SettingsUiEvent.DeleteWallpaper(wallpaper.id)) },
                    )
                }
                item {
                    AddTile(
                        isImporting = isImporting,
                        onClick = onLaunchPicker,
                    )
                }
            }
            if (errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                WallpaperErrorRow(
                    message = errorMessage,
                    onDismiss = { onEvent(SettingsUiEvent.DismissWallpaperError) },
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Layered behind the aurora and dimmed for legibility. Pick None for pure aurora.",
                style = PomtomTheme.typography.body.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = PomtomTheme.colors.ink3,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun NoneTile(selected: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val borderColor = if (selected) colors.ink else colors.ink3.copy(alpha = 0.30f)
    val borderWidth by animateDpAsState(targetValue = if (selected) 2.dp else 1.dp, label = "wallpaper-none-border")
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 116.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(colors.bg2, colors.bg3)))
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = "No wallpaper" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.HideImage,
            contentDescription = null,
            tint = colors.ink2,
            modifier = Modifier.size(26.dp),
        )
        if (selected) SelectedBadge(modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun WallpaperTile(
    wallpaper: Wallpaper,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = PomtomTheme.colors
    val borderColor = if (selected) colors.ink else colors.ink3.copy(alpha = 0.30f)
    val borderWidth by animateDpAsState(targetValue = if (selected) 2.dp else 1.dp, label = "wallpaper-tile-border")
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 116.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bg2)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(role = Role.RadioButton, onClick = onSelect)
            .semantics { contentDescription = wallpaper.displayName },
    ) {
        AsyncImage(
            model = wallpaper.localPath.asFileUriIfNeeded(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
        )
        if (selected) {
            SelectedBadge(modifier = Modifier.align(Alignment.TopEnd))
        } else {
            DeleteBadge(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun AddTile(isImporting: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 116.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cream)
            .border(
                width = 1.dp,
                color = colors.ink3.copy(alpha = 0.30f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(role = Role.Button, enabled = !isImporting, onClick = onClick)
            .semantics { contentDescription = "Add wallpaper" },
        contentAlignment = Alignment.Center,
    ) {
        if (isImporting) {
            CircularProgressIndicator(
                color = colors.ink2,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = colors.ink2,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Add",
                    style = PomtomTheme.typography.mono.copy(fontSize = 9.sp, letterSpacing = 1.6.sp),
                    color = colors.ink3,
                )
            }
        }
    }
}

@Composable
private fun SelectedBadge(modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    Box(
        modifier = modifier
            .padding(6.dp)
            .size(22.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.ink),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = colors.surface,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun DeleteBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    Box(
        modifier = modifier
            .padding(6.dp)
            .size(22.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.bg0.copy(alpha = 0.65f))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Delete wallpaper" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            tint = colors.surface,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun WallpaperErrorRow(message: String, onDismiss: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ember.copy(alpha = 0.12f))
            .border(width = 1.dp, color = colors.ember.copy(alpha = 0.40f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = PomtomTheme.typography.body.copy(fontSize = 11.sp),
            color = colors.ink2,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Dismiss",
                tint = colors.ink3,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun String.asFileUriIfNeeded(): String =
    if (startsWith("file:") || startsWith("content:") || startsWith("http")) this
    else "file://$this"

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = PomtomTheme.colors
    Column {
        Text(
            text = title.uppercase(),
            style = PomtomTheme.typography.mono.copy(
                fontSize = 10.sp,
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.ink3,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.cream)
                .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp)),
            content = content,
        )
    }
}

@Composable
private fun Divider() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.ink3.copy(alpha = 0.20f)),
    )
}

@Composable
private fun DurationRow(label: String, value: String, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = PomtomTheme.typography.mono.copy(fontSize = 11.sp),
            color = colors.ink3,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.ink3,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                color = colors.ink,
            )
            if (description != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = PomtomTheme.typography.body.copy(fontSize = 11.sp),
                    color = colors.ink3,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        ToggleSwitch(checked = checked)
    }
}

@Composable
private fun ToggleSwitch(checked: Boolean) {
    val colors = PomtomTheme.colors
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 21.dp else 3.dp,
        label = "toggle-thumb",
    )
    val trackColor = if (checked) colors.ink else colors.ink.copy(alpha = 0.12f)
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor)
            .semantics { contentDescription = if (checked) "On" else "Off" },
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset, top = 3.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(colors.surface),
        )
    }
}

@Composable
private fun StrictNote() {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.amber.copy(alpha = 0.10f))
            .border(width = 1.dp, color = colors.amber.copy(alpha = 0.30f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = "NOTE",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 10.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = colors.ember,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Apps can\u2019t fully block OS-level switching. We log broken sessions so you can see the truth.",
            style = PomtomTheme.typography.body.copy(fontSize = 11.sp, lineHeight = 16.sp),
            color = colors.ink2,
        )
    }
}

@Composable
private fun ThemeGrid(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    val themes = listOf(
        ThemeOption(AppTheme.DUSK, DuskPalette, "Dusk"),
        ThemeOption(AppTheme.PAPER, PaperPalette, "Paper"),
        ThemeOption(AppTheme.FOREST, ForestPalette, "Forest"),
        ThemeOption(AppTheme.ROSE, RosePalette, "Rose"),
    )
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        themes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    ThemeCard(
                        option = option,
                        selected = option.id == selected,
                        onClick = { onSelect(option.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private data class ThemeOption(val id: AppTheme, val palette: PomtomColors, val label: String)

@Composable
private fun ThemeCard(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parent = PomtomTheme.colors
    val swatch = option.palette
    val borderColor = if (selected) parent.ink else parent.ink3.copy(alpha = 0.30f)
    val borderWidth by animateDpAsState(targetValue = if (selected) 2.dp else 1.dp, label = "theme-border")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(swatch.bg0, swatch.bg2)))
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = "${option.label} theme" }
            .padding(14.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(swatch.amber),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = option.label,
                style = PomtomTheme.typography.titleSerif.copy(
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                ),
                color = swatch.ink,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(parent.ink),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = parent.surface,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

@Composable
private fun Footer() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "POMTOM v0.1 \u00B7 MADE SLOWLY",
            style = PomtomTheme.typography.mono.copy(
                fontSize = 9.sp,
                letterSpacing = 2.7.sp,
            ),
            color = colors.ink3,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationEditorSheet(
    field: DurationField,
    sessionConfig: SessionConfig,
    onDismiss: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = PomtomTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.ink,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = field.title(),
                style = PomtomTheme.typography.titleSerif.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
                color = colors.ink,
            )
            Spacer(Modifier.height(16.dp))
            when (field) {
                DurationField.FOCUS -> SettingsViewModel.FocusOptions.forEach { d ->
                    OptionRow("${d.inWholeMinutes} min", d == sessionConfig.focus) {
                        onEvent(SettingsUiEvent.SetFocus(d))
                    }
                }
                DurationField.SHORT_BREAK -> SettingsViewModel.ShortBreakOptions.forEach { d ->
                    OptionRow("${d.inWholeMinutes} min", d == sessionConfig.shortBreak) {
                        onEvent(SettingsUiEvent.SetShortBreak(d))
                    }
                }
                DurationField.LONG_BREAK -> SettingsViewModel.LongBreakOptions.forEach { d ->
                    OptionRow("${d.inWholeMinutes} min", d == sessionConfig.longBreak) {
                        onEvent(SettingsUiEvent.SetLongBreak(d))
                    }
                }
                DurationField.CYCLES_BEFORE_LONG -> SettingsViewModel.CycleOptions.forEach { c ->
                    OptionRow("$c cycles", c == sessionConfig.cyclesBeforeLong) {
                        onEvent(SettingsUiEvent.SetCyclesBeforeLong(c))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun DurationField.title(): String = when (this) {
    DurationField.FOCUS -> "Focus"
    DurationField.SHORT_BREAK -> "Short break"
    DurationField.LONG_BREAK -> "Long break"
    DurationField.CYCLES_BEFORE_LONG -> "Long break every"
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) colors.amber.copy(alpha = 0.12f) else Color.Transparent,
            )
            .border(
                width = 1.dp,
                color = if (selected) colors.amber.copy(alpha = 0.6f) else colors.ink3.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PomtomTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.amber,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}
