package com.example.processrecord.ui.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * 图片工具类
 *
 * 解决问题：直接存储 content:// URI 在 App 重装或系统清理后会失效。
 * 解决方案：将图片复制到 App 私有目录（filesDir/images/），存储本地绝对路径。
 *
 * 私有目录特点：
 * - 路径：/data/data/<packageName>/files/images/
 * - App 卸载时自动清理
 * - 无需额外存储权限（Android 10+）
 * - 不受 content:// URI 权限失效影响
 */
object ImageUtils {

    private const val IMAGE_DIR = "images"

    /**
     * 将 content:// URI 指向的图片复制到 App 私有目录。
     *
     * @param context 上下文
     * @param sourceUri 来源 URI（content:// 或 file://）
     * @return 复制后的本地绝对路径，失败时返回 null
     */
    suspend fun copyImageToPrivateStorage(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imageDir = File(context.filesDir, IMAGE_DIR).also { it.mkdirs() }
                // 生成唯一文件名，保留原始扩展名（如 .jpg/.png）
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
     * 批量复制图片到私有目录。
     * 已经是本地路径（以 "/" 开头）的图片直接跳过，不重复复制。
     *
     * @param context 上下文
     * @param uris URI 列表（可混合 content:// 和本地路径）
     * @return 本地绝对路径列表（过滤掉复制失败的项）
     */
    suspend fun copyImagesToPrivateStorage(context: Context, uris: List<String>): List<String> {
        return uris.mapNotNull { uriString ->
            if (isLocalPath(uriString)) {
                // 已经是本地路径，直接保留
                uriString
            } else {
                // content:// URI，复制到私有目录
                val uri = Uri.parse(uriString)
                copyImageToPrivateStorage(context, uri)
            }
        }
    }

    /**
     * 删除私有目录中的图片文件。
     * 仅删除位于 App 私有目录内的文件，外部 URI 不处理。
     *
     * @param imagePath 本地绝对路径
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
     * 判断字符串是否为本地文件路径（以 "/" 开头）。
     */
    fun isLocalPath(path: String): Boolean = path.startsWith("/")

    /**
     * 从 URI 推断文件扩展名。
     * 优先从 MIME 类型推断，其次从 URI 路径提取。
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
        // 从 URI 路径提取扩展名
        return uri.lastPathSegment?.substringAfterLast(".", "jpg")
    }
}
