package com.kapdroid.pomtom

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBootstrap(content: @Composable () -> Unit) {
    content()
}
