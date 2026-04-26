package com.kapdroid.pomtom

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kapdroid.pomtom.platform.ActivityHolder
import org.koin.compose.koinInject

@Composable
actual fun PlatformBootstrap(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findComponentActivity() }
    val holder: ActivityHolder = koinInject()
    DisposableEffect(activity, holder) {
        if (activity != null) holder.activity = activity
        onDispose {
            if (holder.activity === activity) holder.activity = null
        }
    }
    content()
}

private fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
