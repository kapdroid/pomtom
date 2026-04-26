package com.kapdroid.pomtom.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class PomtomShapes(
    val pill: RoundedCornerShape,
    val card: RoundedCornerShape,
    val chip: RoundedCornerShape,
    val sheet: RoundedCornerShape,
    val button: RoundedCornerShape,
)

val LocalPomtomShapes = staticCompositionLocalOf<PomtomShapes> {
    error("PomtomShapes not provided. Wrap your composable in PomtomTheme.")
}

internal fun defaultPomtomShapes(): PomtomShapes = PomtomShapes(
    pill = RoundedCornerShape(percent = 50),
    card = RoundedCornerShape(18.dp),
    chip = RoundedCornerShape(14.dp),
    sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    button = RoundedCornerShape(percent = 50),
)
