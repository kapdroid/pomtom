package com.kapdroid.pomtom.filepicker

import androidx.compose.runtime.Composable

interface FilePickerLauncher {
    fun launch()
}

enum class FileKind { AUDIO, IMAGE }

@Composable
expect fun rememberFilePicker(
    kind: FileKind,
    onPicked: (Result<PickedFile>) -> Unit,
): FilePickerLauncher
