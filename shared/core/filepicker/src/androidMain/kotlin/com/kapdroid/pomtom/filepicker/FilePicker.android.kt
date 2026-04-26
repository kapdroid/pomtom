package com.kapdroid.pomtom.filepicker

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
actual fun rememberFilePicker(
    kind: FileKind,
    onPicked: (Result<PickedFile>) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val mimeFilters = remember(kind) { mimeFiltersFor(kind) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) {
            onPicked(Result.failure(PickerCancelledException))
            return@rememberLauncherForActivityResult
        }
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }.onFailure { Napier.w(it) { "Could not persist URI permission for $uri" } }

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { copyToSandbox(context, uri, kind) }
            }
            onPicked(result)
        }
    }

    return remember(launcher, mimeFilters) {
        object : FilePickerLauncher {
            override fun launch() = launcher.launch(mimeFilters)
        }
    }
}

private fun mimeFiltersFor(kind: FileKind): Array<String> = when (kind) {
    FileKind.AUDIO -> arrayOf("audio/*")
    FileKind.IMAGE -> arrayOf("image/*")
}

private fun copyToSandbox(context: Context, uri: Uri, kind: FileKind): PickedFile {
    val resolver = context.contentResolver
    val (displayName, sizeFromCursor) = queryDocument(resolver, uri)
    val mime = resolver.getType(uri)
    val extension = displayName.substringAfterLast('.', missingDelimiterValue = "")
        .ifEmpty { defaultExtensionFor(mime, kind) }

    val targetDir = File(context.filesDir, kind.directoryName).apply { mkdirs() }
    val targetFile = File(targetDir, "${UUID.randomUUID()}.$extension")

    resolver.openInputStream(uri)?.use { input ->
        targetFile.outputStream().use { output -> input.copyTo(output) }
    } ?: error("Cannot open content stream for $uri")

    val sizeBytes = if (sizeFromCursor > 0L) sizeFromCursor else targetFile.length()
    return PickedFile(
        displayName = displayName,
        mimeType = mime,
        absolutePath = targetFile.absolutePath,
        sizeBytes = sizeBytes,
    )
}

private fun queryDocument(resolver: ContentResolver, uri: Uri): Pair<String, Long> {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    resolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
            val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
            return (name ?: uri.lastPathSegment ?: "import") to size
        }
    }
    return (uri.lastPathSegment ?: "import") to 0L
}

private fun defaultExtensionFor(mime: String?, kind: FileKind): String = when {
    mime == "audio/mpeg" -> "mp3"
    mime == "audio/ogg" -> "ogg"
    mime == "audio/opus" -> "opus"
    mime == "audio/wav" || mime == "audio/x-wav" -> "wav"
    mime == "audio/aac" -> "aac"
    mime == "audio/flac" -> "flac"
    mime == "image/jpeg" -> "jpg"
    mime == "image/png" -> "png"
    mime == "image/webp" -> "webp"
    kind == FileKind.AUDIO -> "audio"
    else -> "bin"
}

private val FileKind.directoryName: String
    get() = when (this) {
        FileKind.AUDIO -> "imported_audio"
        FileKind.IMAGE -> "imported_images"
    }
