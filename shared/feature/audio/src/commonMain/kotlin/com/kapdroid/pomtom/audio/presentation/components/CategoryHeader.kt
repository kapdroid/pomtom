package com.kapdroid.pomtom.audio.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kapdroid.pomtom.designsystem.theme.PomtomTheme
import com.kapdroid.pomtom.domain.entity.AudioCategory

@Composable
fun CategoryHeader(category: AudioCategory, count: Int, modifier: Modifier = Modifier) {
    val colors = PomtomTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category.displayName.uppercase(),
            style = PomtomTheme.typography.caption,
            color = colors.ink3,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "${if (count > 0) "·" else ""}",
            style = PomtomTheme.typography.caption,
            color = colors.ink3,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$count",
            style = PomtomTheme.typography.caption,
            color = colors.ink3,
        )
    }
}

private val AudioCategory.displayName: String
    get() = when (this) {
        AudioCategory.AMBIENT -> "Ambient"
        AudioCategory.NATURE -> "Nature"
        AudioCategory.INSTRUMENT -> "Instrument"
    }
