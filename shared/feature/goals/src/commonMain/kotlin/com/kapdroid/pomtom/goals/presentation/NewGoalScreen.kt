package com.kapdroid.pomtom.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.theme.LocalPomtomColors
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.theme.palettes.PaperPalette
import com.kapdroid.pomtom.domain.entity.GoalColor
import com.kapdroid.pomtom.domain.entity.GoalType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewGoalScreen(
    onBack: () -> Unit,
    viewModel: NewGoalViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedId) {
        if (state.savedId != null) {
            viewModel.consumeSaved()
            onBack()
        }
    }
    CompositionLocalProvider(LocalPomtomColors provides PaperPalette) {
        NewGoalContent(
            state = state,
            onBack = onBack,
            onTitle = { viewModel.onEvent(NewGoalUiEvent.TitleChanged(it)) },
            onTargetDelta = { viewModel.onEvent(NewGoalUiEvent.TargetDelta(it)) },
            onType = { viewModel.onEvent(NewGoalUiEvent.TypeSelected(it)) },
            onColor = { viewModel.onEvent(NewGoalUiEvent.ColorSelected(it)) },
            onToggleAttach = { viewModel.onEvent(NewGoalUiEvent.ToggleAttachOnSave) },
            onSave = { viewModel.onEvent(NewGoalUiEvent.Save) },
            onDismissError = { viewModel.onEvent(NewGoalUiEvent.DismissError) },
        )
    }
}

@Composable
private fun NewGoalContent(
    state: NewGoalUiState,
    onBack: () -> Unit,
    onTitle: (String) -> Unit,
    onTargetDelta: (Int) -> Unit,
    onType: (GoalType) -> Unit,
    onColor: (GoalColor) -> Unit,
    onToggleAttach: () -> Unit,
    onSave: () -> Unit,
    onDismissError: () -> Unit,
) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg0)
            .statusBarsPadding()
            .imePadding(),
    ) {
        // Form content centered with a max width — mobile-friendly form rail on tablets.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter),
        ) {
            NewGoalHeader(canSave = state.canSave, isSaving = state.isSaving, onBack = onBack, onSave = onSave)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                if (!state.errorMessage.isNullOrBlank()) {
                    ErrorBanner(message = state.errorMessage, onDismiss = onDismissError)
                }
                TitleSection(value = state.title, onChange = onTitle)
                TargetSection(target = state.target, type = state.type, onDelta = onTargetDelta, onType = onType)
                ColorSection(selected = state.color, onSelect = onColor)
                AttachSection(checked = state.attachOnSave, onToggle = onToggleAttach)
                PreviewSection(state = state)
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun NewGoalHeader(
    canSave: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.30f), shape = RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClickLabel = "Discard", onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.ink)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "NEW GOAL",
            style = PomtomTheme.typography.caption.copy(letterSpacing = 2.6.sp),
            color = colors.ink3,
        )
        Spacer(Modifier.weight(1f))
        SavePill(canSave = canSave, isSaving = isSaving, onSave = onSave)
    }
}

@Composable
private fun SavePill(canSave: Boolean, isSaving: Boolean, onSave: () -> Unit) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    val bg = if (canSave) colors.ink else colors.ink.copy(alpha = 0.30f)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(color = bg, shape = shape)
            .clickable(enabled = canSave, role = Role.Button, onClickLabel = "Save goal", onClick = onSave)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = colors.surface,
                strokeWidth = 2.dp,
                modifier = Modifier.size(14.dp),
            )
        } else {
            Text(
                text = "SAVE",
                style = PomtomTheme.typography.caption.copy(letterSpacing = 2.0.sp),
                color = colors.surface,
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color = colors.ember.copy(alpha = 0.18f), shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = PomtomTheme.typography.caption,
            color = colors.ember,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(role = Role.Button, onClickLabel = "Dismiss", onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.ember)
        }
    }
}

@Composable
private fun TitleSection(value: String, onChange: (String) -> Unit) {
    val colors = PomtomTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Name the",
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 42.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
        Text(
            text = "thing.",
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 42.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        ) {
            val textStyle = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.2).sp,
                color = colors.ink,
            )
            CompositionLocalProvider(LocalTextStyle provides textStyle) {
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(colors.ember),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "Finish the portfolio…",
                                    style = textStyle.copy(color = colors.ink3),
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(color = colors.ink, shape = RoundedCornerShape(1.dp)),
        )
    }
}

@Composable
private fun TargetSection(
    target: Int,
    type: GoalType,
    onDelta: (Int) -> Unit,
    onType: (GoalType) -> Unit,
) {
    val colors = PomtomTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "HOW MUCH")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(color = colors.cream, shape = RoundedCornerShape(20.dp))
                .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.20f), shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepperButton(symbol = "−", onClick = { onDelta(-5) })
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = target.toString(),
                    style = PomtomTheme.typography.timer.copy(
                        fontSize = 64.sp,
                        lineHeight = 64.sp,
                        letterSpacing = (-1.5).sp,
                    ),
                    color = colors.ink,
                )
                Text(
                    text = type.unitLabel().uppercase(),
                    style = PomtomTheme.typography.caption.copy(letterSpacing = 2.6.sp),
                    color = colors.ink3,
                )
            }
            StepperButton(symbol = "+", onClick = { onDelta(5) })
        }
        Spacer(Modifier.height(12.dp))
        TypeSegmented(selected = type, onSelect = onType)
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(color = colors.surface, shape = shape)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.30f), shape = shape)
            .clickable(role = Role.Button, onClickLabel = if (symbol == "+") "Increase" else "Decrease", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = PomtomTheme.typography.titleSerif.copy(fontStyle = FontStyle.Normal, fontSize = 24.sp),
            color = colors.ink,
        )
    }
}

@Composable
private fun TypeSegmented(selected: GoalType, onSelect: (GoalType) -> Unit) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = colors.ink.copy(alpha = 0.04f), shape = shape)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.20f), shape = shape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        GoalType.entries.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(color = if (isSelected) colors.ink else Color.Transparent, shape = shape)
                    .clickable(role = Role.Tab, onClick = { onSelect(type) })
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = type.unitLabel().uppercase(),
                    style = PomtomTheme.typography.caption.copy(letterSpacing = 1.8.sp),
                    color = if (isSelected) colors.surface else colors.ink2,
                )
            }
        }
    }
}

@Composable
private fun ColorSection(selected: GoalColor, onSelect: (GoalColor) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(text = "PALETTE")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GoalColor.entries.forEach { color ->
                ColorTile(
                    color = color,
                    isSelected = color == selected,
                    onClick = { onSelect(color) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ColorTile(
    color: GoalColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val gradient = color.gradient()
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(70.dp)
            .clip(shape)
            .background(brush = Brush.linearGradient(listOf(gradient.start, gradient.end)), shape = shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.ink else colors.ink3.copy(alpha = 0.30f),
                shape = shape,
            )
            .clickable(role = Role.Button, onClickLabel = gradient.name, onClick = onClick),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = gradient.name.uppercase(),
            style = PomtomTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 2.0.sp, fontWeight = FontWeight.SemiBold),
            color = colors.surface,
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
}

@Composable
private fun AttachSection(checked: Boolean, onToggle: () -> Unit) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = colors.cream.copy(alpha = 0.7f), shape = shape)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.20f), shape = shape)
            .clickable(role = Role.Switch, onClickLabel = "Attach to next session", onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Attach to next session",
                style = PomtomTheme.typography.bodyEmphasis,
                color = colors.ink,
            )
            Text(
                text = "Sessions you finish will count toward this goal.",
                style = PomtomTheme.typography.caption,
                color = colors.ink3,
            )
        }
        Spacer(Modifier.width(12.dp))
        ToggleTrack(checked = checked)
    }
}

@Composable
private fun ToggleTrack(checked: Boolean) {
    val colors = PomtomTheme.colors
    val trackShape = RoundedCornerShape(percent = 50)
    val trackColor = if (checked) colors.ember else colors.ink3.copy(alpha = 0.45f)
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(trackShape)
            .background(color = trackColor, shape = trackShape),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(color = colors.surface, shape = RoundedCornerShape(percent = 50)),
        )
    }
}

@Composable
private fun PreviewSection(state: NewGoalUiState) {
    val colors = PomtomTheme.colors
    val gradient = state.color.gradient()
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = colors.cream, shape = shape)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = 0.20f), shape = shape)
            .padding(16.dp),
    ) {
        SectionLabel(text = "PREVIEW")
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush = Brush.linearGradient(listOf(gradient.start, gradient.end)), shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.color.glyph(),
                    style = PomtomTheme.typography.titleSerif.copy(fontStyle = FontStyle.Normal, fontSize = 20.sp),
                    color = colors.surface,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title.ifBlank { "Your goal" },
                    style = PomtomTheme.typography.titleSans.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.ink,
                )
                Text(
                    text = "0/${state.target} ${state.type.unitLabel()}".uppercase(),
                    style = PomtomTheme.typography.caption.copy(letterSpacing = 1.8.sp),
                    color = colors.ink3,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color = colors.ink.copy(alpha = 0.06f), shape = RoundedCornerShape(3.dp)),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = PomtomTheme.colors
    Text(
        text = text,
        style = PomtomTheme.typography.caption.copy(letterSpacing = 2.8.sp),
        color = colors.ink3,
    )
}
