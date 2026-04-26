package com.kapdroid.pomtom.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class Haptics {

    actual fun tap() = onMain {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).apply {
            prepare()
            impactOccurred()
        }
    }

    actual fun success() = onMain {
        UINotificationFeedbackGenerator().apply {
            prepare()
            notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        }
    }

    actual fun warning() = onMain {
        UINotificationFeedbackGenerator().apply {
            prepare()
            notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun onMain(block: () -> Unit) {
        dispatch_async(dispatch_get_main_queue(), block)
    }
}
