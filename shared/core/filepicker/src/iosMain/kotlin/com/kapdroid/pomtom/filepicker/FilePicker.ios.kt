package com.kapdroid.pomtom.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUUID
import platform.Foundation.lastPathComponent
import platform.Foundation.pathExtension
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberFilePicker(
    kind: FileKind,
    onPicked: (Result<PickedFile>) -> Unit,
): FilePickerLauncher = remember(kind, onPicked) {
    IosFilePickerLauncher(kind = kind, onPicked = onPicked)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosFilePickerLauncher(
    private val kind: FileKind,
    private val onPicked: (Result<PickedFile>) -> Unit,
) : FilePickerLauncher {

    private var delegate: PickerDelegate? = null

    override fun launch() {
        val host = topMostViewController() ?: run {
            onPicked(Result.failure(IllegalStateException("No active view controller to present from")))
            return
        }
        val type = when (kind) {
            FileKind.AUDIO -> UTTypeAudio
            FileKind.IMAGE -> UTTypeImage
        }
        val controller = UIDocumentPickerViewController(forOpeningContentTypes = listOf(type))
        val handler = PickerDelegate(kind = kind, onPicked = onPicked) { delegate = null }
        delegate = handler
        controller.delegate = handler
        controller.allowsMultipleSelection = false
        host.presentViewController(controller, animated = true, completion = null)
    }
}

private fun topMostViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val keyWindow = app.windows.firstOrNull { (it as? UIWindow)?.keyWindow == true } as? UIWindow
        ?: app.windows.firstOrNull() as? UIWindow
    var current = keyWindow?.rootViewController
    while (current?.presentedViewController != null) {
        current = current.presentedViewController
    }
    return current
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class PickerDelegate(
    private val kind: FileKind,
    private val onPicked: (Result<PickedFile>) -> Unit,
    private val onComplete: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val sourceUrl = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (sourceUrl == null) {
            onPicked(Result.failure(IllegalStateException("Picker returned no URL")))
            onComplete()
            return
        }
        val secured = sourceUrl.startAccessingSecurityScopedResource()
        try {
            val result = runCatching { copyToSandbox(sourceUrl, kind) }
            onPicked(result)
        } finally {
            if (secured) sourceUrl.stopAccessingSecurityScopedResource()
            onComplete()
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(Result.failure(PickerCancelledException))
        onComplete()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun copyToSandbox(source: NSURL, kind: FileKind): PickedFile {
    val displayName = source.lastPathComponent ?: "import"
    val extension = source.pathExtension?.takeIf { it.isNotEmpty() }
        ?: defaultExtensionFor(kind)

    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    ) ?: error("Could not resolve documents directory")

    val targetDir = documents.URLByAppendingPathComponent(kind.directoryName)
        ?: error("Could not derive target directory")
    NSFileManager.defaultManager.createDirectoryAtURL(
        targetDir,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )

    val fileName = "${NSUUID().UUIDString}.$extension"
    val target = targetDir.URLByAppendingPathComponent(fileName)
        ?: error("Could not derive target file path")

    val targetPath = target.path ?: error("Target URL has no path")
    if (NSFileManager.defaultManager.fileExistsAtPath(targetPath)) {
        NSFileManager.defaultManager.removeItemAtURL(target, error = null)
    }

    memScoped {
        val errorVar = alloc<ObjCObjectVar<NSError?>>()
        val copied = NSFileManager.defaultManager.copyItemAtURL(
            srcURL = source,
            toURL = target,
            error = errorVar.ptr,
        )
        if (!copied) {
            val message = errorVar.value?.localizedDescription ?: "copy failed"
            Napier.w { "Picker copy failed: $message" }
            error("Could not copy picked file: $message")
        }
    }

    val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(targetPath, error = null)
    val sizeBytes = (attrs?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L

    return PickedFile(
        displayName = displayName,
        mimeType = null,
        absolutePath = targetPath,
        sizeBytes = sizeBytes,
    )
}

private fun defaultExtensionFor(kind: FileKind): String = when (kind) {
    FileKind.AUDIO -> "audio"
    FileKind.IMAGE -> "img"
}

private val FileKind.directoryName: String
    get() = when (this) {
        FileKind.AUDIO -> "imported_audio"
        FileKind.IMAGE -> "imported_images"
    }
