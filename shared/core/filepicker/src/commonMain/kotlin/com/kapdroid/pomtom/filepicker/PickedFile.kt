package com.kapdroid.pomtom.filepicker

data class PickedFile(
    val displayName: String,
    val mimeType: String?,
    val absolutePath: String,
    val sizeBytes: Long,
)
