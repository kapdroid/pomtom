package com.kapdroid.pomtom.goals.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapdroid.pomtom.designsystem.theme.LocalPomtomColors
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.designsystem.theme.palettes.PaperPalette
import com.kapdroid.pomtom.domain.entity.Goal
import com.kapdroid.pomtom.domain.entity.GoalAttachMode
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalsScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalPomtomColors provides PaperPalette) {
        GoalsScreenContent(
            state = state,
            onBack = onBack,
            onCreate = onCreate,
            onSelectFilter = { viewModel.onEvent(GoalsUiEvent.SelectFilter(it)) },
            onToggleAttach = { viewModel.onEvent(GoalsUiEvent.ToggleAttach(it)) },
            onDelete = { viewModel.onEvent(GoalsUiEvent.Delete(it)) },
        )
    }
}

@Composable
private fun GoalsScreenContent(
    state: GoalsUiState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onSelectFilter: (GoalsFilter) -> Unit,
    onToggleAttach: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg0),
    ) {
        // Max-720dp content column centered inside the bg0 box — caps line length on
        // tablets and landscape phones; portrait phones (< 720dp wide) are unaffected.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        ) {
            GoalsHeader(onBack = onBack, onCreate = onCreate)
            GoalsHero(activeCount = state.activeCount, totalSessions = state.totalSessions)
            FilterRow(selected = state.filter, onSelect = onSelectFilter)
            Spacer(Modifier.height(8.dp))
            when {
                state.isLoading -> LoadingBlock()
                state.visible.isEmpty() -> EmptyBlock(
                    filter = state.filter,
                    hasAny = state.all.isNotEmpty(),
                    onCreate = onCreate,
                )
                else -> GoalCardList(
                    goals = state.visible,
                    onToggleAttach = onToggleAttach,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun GoalsHeader(onBack: () -> Unit, onCreate: () -> Unit) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircleButton(onClick = onBack, contentDescription = "Back", inverted = false) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = colors.ink)
        }
        Spacer(Modifier.weight(1f))
        IconCircleButton(onClick = onCreate, contentDescription = "Create goal", inverted = true) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.surface)
        }
    }
}

@Composable
private fun IconCircleButton(
    onClick: () -> Unit,
    contentDescription: String,
    inverted: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val background = if (inverted) colors.ink else colors.ink.copy(alpha = 0.04f)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(color = background, shape = shape)
            .border(width = 1.dp, color = colors.ink3.copy(alpha = if (inverted) 0f else 0.18f), shape = shape)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun GoalsHero(activeCount: Int, totalSessions: Int) {
    val colors = PomtomTheme.colors
    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
        Text(
            text = "What you're",
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
        Text(
            text = "working toward.",
            style = PomtomTheme.typography.display.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = colors.ink,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "$activeCount active · $totalSessions sessions committed".uppercase(),
            style = PomtomTheme.typography.caption.copy(letterSpacing = 2.4.sp),
            color = colors.ink3,
        )
    }
}

@Composable
private fun FilterRow(selected: GoalsFilter, onSelect: (GoalsFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoalsFilter.entries.forEach { filter ->
            FilterPill(
                label = filter.label,
                selected = filter == selected,
                onClick = { onSelect(filter) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PomtomTheme.colors
    val background by animateColorAsState(
        targetValue = if (selected) colors.ink else Color.Transparent,
        label = "filter-bg",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) colors.surface else colors.ink2,
        label = "filter-fg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.ink else colors.ink3.copy(alpha = 0.35f),
        label = "filter-border",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color = background, shape = RoundedCornerShape(percent = 50))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(percent = 50))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = PomtomTheme.typography.caption.copy(letterSpacing = 2.0.sp),
            color = foreground,
        )
    }
}

@Composable
private fun GoalCardList(
    goals: List<Goal>,
    onToggleAttach: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = goals, key = { it.id }) { goal ->
            GoalCard(
                goal = goal,
                onToggleAttach = { onToggleAttach(goal.id) },
                onDelete = { onDelete(goal.id) },
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onToggleAttach: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = PomtomTheme.colors
    val gradient = goal.color.gradient()
    val isAttached = goal.attachMode == GoalAttachMode.NEXT_SESSION
    val isDone = goal.isCompleted
    val cardShape = RoundedCornerShape(20.dp)
    val borderColor = when {
        isDone -> colors.sage.copy(alpha = 0.5f)
        isAttached -> gradient.start.copy(alpha = 0.85f)
        else -> colors.ink3.copy(alpha = 0.20f)
    }
    val borderWidth = if (isAttached || isDone) 2.dp else 1.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(color = colors.cream, shape = cardShape)
            .border(width = borderWidth, color = borderColor, shape = cardShape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            GoalAvatar(gradient = gradient, glyph = goal.color.glyph())
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = goal.title,
                        style = PomtomTheme.typography.titleSans.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.ink,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    PercentLabel(percent = goal.percent)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip(text = goal.color.gradient().name.uppercase())
                    Chip(text = "${goal.progress}/${goal.target} ${goal.type.unitLabel()}")
                    if (isDone) Chip(text = "DONE")
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        ProgressBar(percent = goal.percent, gradient = gradient)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DeleteButton(onClick = onDelete)
            AttachButton(isAttached = isAttached, onClick = onToggleAttach)
        }
    }
}

@Composable
private fun GoalAvatar(gradient: GoalGradient, glyph: String) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(brush = Brush.linearGradient(listOf(gradient.start, gradient.end)), shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style = PomtomTheme.typography.titleSerif.copy(fontStyle = FontStyle.Normal, fontSize = 20.sp),
            color = colors.surface,
        )
    }
}

@Composable
private fun PercentLabel(percent: Float) {
    val colors = PomtomTheme.colors
    val pct = (percent * 100f).toInt()
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = pct.toString(),
            style = PomtomTheme.typography.titleSerif.copy(
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 22.sp,
            ),
            color = colors.ink,
        )
        Text(
            text = "%",
            style = PomtomTheme.typography.caption,
            color = colors.ink3,
            modifier = Modifier.padding(start = 1.dp, bottom = 2.dp),
        )
    }
}

@Composable
private fun Chip(text: String) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = colors.ink.copy(alpha = 0.06f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = PomtomTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 1.4.sp),
            color = colors.ink2,
        )
    }
}

@Composable
private fun ProgressBar(percent: Float, gradient: GoalGradient) {
    val colors = PomtomTheme.colors
    val animated by animateFloatAsState(targetValue = percent.coerceIn(0f, 1f), label = "progress-fill")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color = colors.ink.copy(alpha = 0.06f), shape = RoundedCornerShape(3.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animated)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush = Brush.horizontalGradient(listOf(gradient.start, gradient.end))),
        )
    }
}

@Composable
private fun AttachButton(isAttached: Boolean, onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    val bg = if (isAttached) colors.ink else Color.Transparent
    val fg = if (isAttached) colors.surface else colors.ink
    Row(
        modifier = Modifier
            .clip(shape)
            .background(color = bg, shape = shape)
            .border(
                width = 1.dp,
                color = if (isAttached) Color.Transparent else colors.ink,
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isAttached) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = "ATTACHED",
                style = PomtomTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 2.2.sp),
                color = fg,
            )
        } else {
            Text(
                text = "ATTACH TO NEXT",
                style = PomtomTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 2.2.sp),
                color = fg,
            )
        }
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(percent = 50))
            .clickable(role = Role.Button, onClickLabel = "Delete goal", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.DeleteOutline,
            contentDescription = null,
            tint = colors.ink3,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun LoadingBlock() {
    val colors = PomtomTheme.colors
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.ember, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyBlock(filter: GoalsFilter, hasAny: Boolean, onCreate: () -> Unit) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val title = when {
            !hasAny -> "Nothing yet."
            filter == GoalsFilter.ACTIVE -> "All caught up."
            filter == GoalsFilter.DONE -> "Nothing finished — yet."
            else -> "Nothing to show."
        }
        val sub = when {
            !hasAny -> "A goal is a quiet promise. Make one."
            filter == GoalsFilter.ACTIVE -> "Every goal is done. Take a breath."
            else -> "Your finished goals will live here."
        }
        Text(
            text = title,
            style = PomtomTheme.typography.titleSerif,
            color = colors.ink,
        )
        Text(
            text = sub,
            style = PomtomTheme.typography.body,
            color = colors.ink3,
        )
        if (!hasAny) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(color = colors.ink, shape = RoundedCornerShape(percent = 50))
                    .clickable(role = Role.Button, onClick = onCreate)
                    .padding(horizontal = 22.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "MAKE ONE",
                    style = PomtomTheme.typography.caption.copy(letterSpacing = 2.4.sp),
                    color = colors.surface,
                )
            }
        }
    }
}
