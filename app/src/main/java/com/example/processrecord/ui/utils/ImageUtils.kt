package com.example.processrecord.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Utilities for copying user-selected images into app-private storage.
 *
 * Why this exists:
 * - Persisting `content://` URIs directly is fragile after reinstall/cleanup.
 * - We copy images into `filesDir/images` and store absolute local paths.
 *
 * Private storage behavior:
 * - Path: `data/data/<package>/files/images`
 * - Removed automatically on app uninstall
 * - No external storage permission required on modern Android
 */
object ImageUtils {

    private const val IMAGE_DIR = "images"

    /**
     * Copy one source URI into app-private storage and return local absolute path.
     *
     * Returns `null` if read/copy fails.
     */
    suspend fun copyImageToPrivateStorage(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imageDir = File(context.filesDir, IMAGE_DIR).also { it.mkdirs() }
                // Keep a reasonable extension when available.
                val extension = getExtensionFromUri(context, sourceUri) ?: "jpg"
                val destFile = File(imageDir, "${UUID.randomUUID()}.$extension")

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: return@withContext null

                destFile.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } catch (e: SecurityException) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Copy a mixed URI list into private storage.
     *
     * Existing local absolute paths (starting with `/`) are kept as-is.
     * Failed copies are filtered out.
     */
    suspend fun copyImagesToPrivateStorage(context: Context, uris: List<String>): List<String> {
        return uris.mapNotNull { uriString ->
            if (isLocalPath(uriString)) {
                uriString
            } else {
                val uri = uriString.toUri()
                copyImageToPrivateStorage(context, uri)
            }
        }
    }

    /**
     * Delete one local image file from private storage if it exists.
     */
    fun deleteImageFromPrivateStorage(imagePath: String) {
        if (isLocalPath(imagePath)) {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     * Return true when a path looks like a local absolute file path.
     */
    fun isLocalPath(path: String): Boolean = path.startsWith("/")

    /**
     * Resolve a reasonable file extension from MIME type or URI path.
     */
    private fun getExtensionFromUri(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            return when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                "image/heic", "image/heif" -> "heic"
                else -> mimeType.substringAfterLast("/", "jpg")
            }
        }
        // Fallback to URI path extension.
        return uri.lastPathSegment?.substringAfterLast(".", "jpg")
    }
}
