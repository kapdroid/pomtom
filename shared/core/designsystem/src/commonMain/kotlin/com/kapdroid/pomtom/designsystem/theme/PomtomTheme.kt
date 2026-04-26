package com.kapdroid.pomtom.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.kapdroid.pomtom.designsystem.theme.palettes.DuskPalette

object PomtomTheme {
    val colors: PomtomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPomtomColors.current

    val typography: PomtomTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPomtomTypography.current

    val shapes: PomtomShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalPomtomShapes.current

    val motion: PomtomMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalPomtomMotion.current
}

@Composable
fun PomtomTheme(
    colors: PomtomColors = DuskPalette,
    typography: PomtomTypography = defaultPomtomTypography(),
    shapes: PomtomShapes = defaultPomtomShapes(),
    motion: PomtomMotion = PomtomMotion(),
    content: @Composable () -> Unit,
) {
    val materialColorScheme = if (colors.isDark) {
        darkColorScheme(
            primary = colors.amber,
            onPrimary = colors.onAccent,
            secondary = colors.ember,
            onSecondary = colors.onAccent,
            tertiary = colors.violet,
            onTertiary = colors.onAccent,
            background = colors.bg0,
            onBackground = colors.ink,
            surface = colors.bg1,
            onSurface = colors.ink,
            surfaceVariant = colors.bg2,
            onSurfaceVariant = colors.ink2,
            outline = colors.ink3,
        )
    } else {
        lightColorScheme(
            primary = colors.amber,
            onPrimary = colors.onAccent,
            secondary = colors.ember,
            onSecondary = colors.onAccent,
            tertiary = colors.violet,
            onTertiary = colors.onAccent,
            background = colors.bg0,
            onBackground = colors.ink,
            surface = colors.surface,
            onSurface = colors.ink,
            surfaceVariant = colors.surface2,
            onSurfaceVariant = colors.ink2,
            outline = colors.ink3,
        )
    }
    CompositionLocalProvider(
        LocalPomtomColors provides colors,
        LocalPomtomTypography provides typography,
        LocalPomtomShapes provides shapes,
        LocalPomtomMotion provides motion,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography.toMaterialTypography(),
            content = content,
        )
    }
}
