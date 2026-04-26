package com.kapdroid.pomtom.platform

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

actual class Haptics(private val activityHolder: ActivityHolder) {

    actual fun tap() = perform(HapticFeedbackConstants.VIRTUAL_KEY)

    actual fun success() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.LONG_PRESS,
    )

    actual fun warning() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.CONTEXT_CLICK,
    )

    private fun perform(constant: Int) {
        val view: View = activityHolder.activity?.window?.decorView ?: return
        view.performHapticFeedback(constant)
    }
}
