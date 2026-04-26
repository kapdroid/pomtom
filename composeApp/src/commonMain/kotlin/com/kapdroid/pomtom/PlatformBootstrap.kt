package com.kapdroid.pomtom

import androidx.compose.runtime.Composable

@Composable
expect fun PlatformBootstrap(content: @Composable () -> Unit)
