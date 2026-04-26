package com.kapdroid.pomtom.onboarding.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.kapdroid.pomtom.designsystem.util.WithWindowMetrics
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private data class OnboardingPage(
    val titleStart: String,
    val titleAccent: String,
    val body: String,
    val art: @Composable () -> Unit,
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val pages = onboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isCompleting by viewModel.isCompleting.collectAsStateWithLifecycle()

    AuroraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
        ) {
            BrandRow(onSkip = { viewModel.complete(onComplete) }, skipEnabled = !isCompleting)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { index ->
                OnboardingPageContent(page = pages[index])
            }
            FooterControls(
                pageIndex = pagerState.currentPage,
                pageCount = pages.size,
                isCompleting = isCompleting,
                onAdvance = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        viewModel.complete(onComplete)
                    }
                },
            )
        }
    }
}

@Composable
private fun onboardingPages(): List<OnboardingPage> = remember {
    listOf(
        OnboardingPage(
            titleStart = "A room",
            titleAccent = "for work.",
            body = "No gamification. No streaks shaming you. Just a quiet timer, a goal to aim at, and something good in your ears.",
            art = { OnboardArtTimer() },
        ),
        OnboardingPage(
            titleStart = "Name the",
            titleAccent = "thing.",
            body = "Attach a goal to any session. Thirty minutes at a time, the thing gets done.",
            art = { OnboardArtGoals() },
        ),
        OnboardingPage(
            titleStart = "Strict, if",
            titleAccent = "you need.",
            body = "Fullscreen, wake-lock, a three-second hold to escape. Or keep it soft. Your call.",
            art = { OnboardArtStrict() },
        ),
    )
}

@Composable
private fun BrandRow(onSkip: () -> Unit, skipEnabled: Boolean) {
    val colors = PomtomTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Brush.linearGradient(listOf(colors.amber, colors.ember))),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "PomTom",
                style = PomtomTheme.typography.titleSerif.copy(fontSize = 18.sp),
                color = colors.ink,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .clickable(role = Role.Button, enabled = skipEnabled, onClick = onSkip)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .semantics { contentDescription = "Skip onboarding" },
        ) {
            Text(
                text = "SKIP \u2192",
                style = PomtomTheme.typography.mono.copy(
                    fontSize = 10.sp,
                    letterSpacing = 2.4.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink3,
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    WithWindowMetrics { metrics ->
        if (metrics.isLandscape) OnboardingPageLandscape(page) else OnboardingPagePortrait(page)
    }
}

@Composable
private fun OnboardingPagePortrait(page: OnboardingPage) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { page.art() }
        Spacer(Modifier.height(30.dp))
        OnboardingHeadline(page)
        Spacer(Modifier.height(16.dp))
        OnboardingBody(page)
    }
}

// Landscape onboarding: illustration left, copy right. Each gets equal weight so the
// art keeps its presence while the headline + body get the room to breathe.
@Composable
private fun OnboardingPageLandscape(page: OnboardingPage) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { page.art() }
        Spacer(Modifier.width(24.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            OnboardingHeadline(page)
            Spacer(Modifier.height(16.dp))
            OnboardingBody(page)
        }
    }
}

@Composable
private fun OnboardingHeadline(page: OnboardingPage) {
    val colors = PomtomTheme.colors
    Text(
        text = page.titleStart,
        style = PomtomTheme.typography.display.copy(
            fontSize = 48.sp,
            lineHeight = 50.sp,
            fontWeight = FontWeight.Normal,
        ),
        color = colors.ink,
    )
    Text(
        text = page.titleAccent,
        style = PomtomTheme.typography.display.copy(
            fontSize = 48.sp,
            lineHeight = 50.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
        ),
        color = colors.amber,
    )
}

@Composable
private fun OnboardingBody(page: OnboardingPage) {
    val colors = PomtomTheme.colors
    Text(
        text = page.body,
        style = PomtomTheme.typography.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
        color = colors.ink2,
        modifier = Modifier.fillMaxWidth(0.9f),
    )
}

@Composable
private fun FooterControls(
    pageIndex: Int,
    pageCount: Int,
    isCompleting: Boolean,
    onAdvance: () -> Unit,
) {
    val colors = PomtomTheme.colors
    Column(modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(pageCount) { i ->
                val filled = i <= pageIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.ink3.copy(alpha = 0.18f)),
                ) {
                    if (filled) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(colors.amber, colors.ember))),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        val label = if (pageIndex < pageCount - 1) "Continue" else "Enter the room"
        // Button colors must contrast with the page bg AND with each other. On dark
        // themes, `surface` is the light cream and `bg0` is dark — readable. On
        // light themes (Paper, Sakura) `bg0` is also light, so cream-on-light is
        // invisible — flip to a dark `ink` background with light `surface` text.
        val buttonBg = if (colors.isDark) colors.surface else colors.ink
        val buttonFg = if (colors.isDark) colors.bg0 else colors.surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .background(buttonBg)
                .clickable(role = Role.Button, enabled = !isCompleting, onClick = onAdvance)
                .padding(vertical = 18.dp)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = PomtomTheme.typography.body.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                    ),
                    color = buttonFg,
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = buttonFg,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardArtTimer() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringStroke = 1.dp.toPx()
            listOf(100f, 80f, 60f).forEachIndexed { i, r ->
                drawCircle(
                    color = colors.ink3.copy(alpha = 0.10f + i * 0.05f),
                    radius = r.dp.toPx(),
                    center = center,
                    style = Stroke(width = ringStroke),
                )
            }
            val sweepRadius = 100.dp.toPx()
            val brush = Brush.linearGradient(listOf(colors.amber, colors.ember))
            drawArc(
                brush = brush,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - sweepRadius, center.y - sweepRadius),
                size = androidx.compose.ui.geometry.Size(sweepRadius * 2, sweepRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "25:00",
                style = PomtomTheme.typography.display.copy(
                    fontSize = 64.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = colors.ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "FOCUS",
                style = PomtomTheme.typography.mono.copy(
                    fontSize = 9.sp,
                    letterSpacing = 3.6.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.amber,
            )
        }
    }
}

@Composable
private fun OnboardArtGoals() {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoalChip(label = "THESIS \u00B7 CH. 4", percent = 70, ratio = 0.80f, gradient = listOf(colors.amber, colors.ember))
        GoalChip(label = "READ \u00B7 300min", percent = 45, ratio = 0.65f, gradient = listOf(colors.violet, colors.violet.copy(alpha = 0.6f)))
        GoalChip(label = "DAILY FOCUS", percent = 25, ratio = 0.90f, gradient = listOf(colors.sage, colors.sage.copy(alpha = 0.6f)))
    }
}

@Composable
private fun GoalChip(label: String, percent: Int, ratio: Float, gradient: List<Color>) {
    val colors = PomtomTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bg2.copy(alpha = 0.55f))
            .border(1.dp, colors.ink3.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = PomtomTheme.typography.mono.copy(
                    fontSize = 9.sp,
                    letterSpacing = 2.4.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink,
            )
            Text(
                text = "$percent%",
                style = PomtomTheme.typography.mono.copy(fontSize = 9.sp),
                color = colors.ink3,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.ink3.copy(alpha = 0.10f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(gradient)),
            )
        }
    }
}

@Composable
private fun OnboardArtStrict() {
    val colors = PomtomTheme.colors
    Box(
        modifier = Modifier.size(width = 200.dp, height = 240.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.bg0)
                .border(1.5.dp, colors.amber.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(colors.amber.copy(alpha = 0.18f))
                    .border(1.dp, colors.amber.copy(alpha = 0.50f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "STRICT",
                    style = PomtomTheme.typography.mono.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.amber,
                )
            }
            Text(
                text = "14:30",
                style = PomtomTheme.typography.display.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = colors.ink,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(percent = 50))
                    .border(1.dp, colors.ink3.copy(alpha = 0.25f), RoundedCornerShape(percent = 50)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .background(Brush.horizontalGradient(listOf(colors.ember, colors.amber))),
                ) {
                    Spacer(Modifier.height(28.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "HOLD TO EXIT",
                        style = PomtomTheme.typography.mono.copy(
                            fontSize = 8.sp,
                            letterSpacing = 1.8.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.surface,
                    )
                }
            }
        }
    }
}

