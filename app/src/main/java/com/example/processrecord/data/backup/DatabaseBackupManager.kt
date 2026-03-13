package com.example.processrecord.data.backup

import android.content.Context
import android.net.Uri
import com.example.processrecord.data.AppDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.text.Charsets

private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
private val ZIP_LOCAL_FILE_HEADER = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
private const val IMAGES_DIR_NAME = "images"
private const val ZIP_ENTRY_DB = "database/process_record_database"
private const val ZIP_ENTRY_DB_WAL = "database/process_record_database-wal"
private const val ZIP_ENTRY_DB_SHM = "database/process_record_database-shm"
private const val ZIP_ENTRY_IMAGES_PREFIX = "images/"

internal fun hasSqliteHeader(file: File): Boolean {
    if (!file.exists() || file.length() < SQLITE_HEADER.size.toLong()) return false
    val header = ByteArray(SQLITE_HEADER.size)
    FileInputStream(file).use { input ->
        val readCount = input.read(header)
        if (readCount != SQLITE_HEADER.size) return false
    }
    return header.contentEquals(SQLITE_HEADER)
}

internal fun hasZipHeader(file: File): Boolean {
    if (!file.exists() || file.length() < ZIP_LOCAL_FILE_HEADER.size.toLong()) return false
    val header = ByteArray(ZIP_LOCAL_FILE_HEADER.size)
    FileInputStream(file).use { input ->
        val readCount = input.read(header)
        if (readCount != ZIP_LOCAL_FILE_HEADER.size) return false
    }
    return header.contentEquals(ZIP_LOCAL_FILE_HEADER)
}

internal fun resolveZipEntryPathSafe(destinationDir: File, entryName: String): File {
    val destinationCanonicalFile = destinationDir.canonicalFile
    val outputCanonicalFile = File(destinationCanonicalFile, entryName).canonicalFile

    val destinationCanonicalPath = destinationCanonicalFile.path
    val outputCanonicalPath = outputCanonicalFile.path
    val destinationPrefix = if (destinationCanonicalPath.endsWith(File.separator)) {
        destinationCanonicalPath
    } else {
        destinationCanonicalPath + File.separator
    }

    val isSameDirectory = outputCanonicalPath.equals(destinationCanonicalPath, ignoreCase = true)
    val isChildPath = outputCanonicalPath.startsWith(destinationPrefix, ignoreCase = true)
    if (!isSameDirectory && !isChildPath) {
        throw IOException("备份压缩包路径非法")
    }
    return outputCanonicalFile
}

interface BackupOperations {
    fun exportDatabase(destUri: Uri): Result<String>
    fun importDatabase(sourceUri: Uri): Result<String>
    fun getDefaultBackupFileName(): String
}

class DatabaseBackupManager(private val context: Context) : BackupOperations {

    private val dbName = "process_record_database"

    override fun exportDatabase(destUri: Uri): Result<String> {
        return try {
            AppDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                return Result.failure(Exception("数据库文件不存在"))
            }

            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")
            val imagesDir = File(context.filesDir, IMAGES_DIR_NAME)

            context.contentResolver.openOutputStream(destUri)?.use { output ->
                ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                    addFileToZipIfExists(zip, dbFile, ZIP_ENTRY_DB)
                    addFileToZipIfExists(zip, walFile, ZIP_ENTRY_DB_WAL)
                    addFileToZipIfExists(zip, shmFile, ZIP_ENTRY_DB_SHM)
                    addDirectoryToZipIfExists(zip, imagesDir, ZIP_ENTRY_IMAGES_PREFIX)
                }
            } ?: return Result.failure(Exception("无法打开备份输出流"))

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            Result.success("备份成功（$timestamp）")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun importDatabase(sourceUri: Uri): Result<String> {
        val tempImportFile = File(context.cacheDir, "$dbName.import.tmp")
        val tempExtractDir = File(context.cacheDir, "$dbName.import.extract")
        var dbBackupFile: File? = null
        var walBackupFile: File? = null
        var shmBackupFile: File? = null
        var imagesBackupDir: File? = null
        try {
            AppDatabase.isRestoring = true
            AppDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(dbName)
            dbFile.parentFile?.mkdirs()
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")
            val imagesDir = File(context.filesDir, IMAGES_DIR_NAME)

            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(Exception("无法打开备份文件"))
            inputStream.use { input ->
                FileOutputStream(tempImportFile).use { output ->
                    input.copyTo(output)
                }
            }

            val isZipBackup = hasZipHeader(tempImportFile)
            if (!isZipBackup && !hasSqliteHeader(tempImportFile)) {
                return Result.failure(Exception("备份文件无效或已损坏"))
            }

            dbBackupFile = backupIfExists(dbFile, File(context.cacheDir, "$dbName.db.bak"))
            walBackupFile = backupIfExists(walFile, File(context.cacheDir, "$dbName.wal.bak"))
            shmBackupFile = backupIfExists(shmFile, File(context.cacheDir, "$dbName.shm.bak"))
            if (isZipBackup) {
                imagesBackupDir = backupDirectoryIfExists(imagesDir, File(context.cacheDir, "$dbName.images.bak"))
            }

            try {
                if (isZipBackup) {
                    restoreFromZipBackup(
                        zipFile = tempImportFile,
                        extractDir = tempExtractDir,
                        dbFile = dbFile,
                        walFile = walFile,
                        shmFile = shmFile,
                        imagesDir = imagesDir
                    )
                } else {
                    tempImportFile.copyTo(dbFile, overwrite = true)
                    walFile.delete()
                    shmFile.delete()
                }

                return Result.success("恢复成功，正在重启应用以加载数据")
            } catch (e: Exception) {
                restoreFileOrDelete(dbFile, dbBackupFile)
                restoreFileOrDelete(walFile, walBackupFile)
                restoreFileOrDelete(shmFile, shmBackupFile)
                if (isZipBackup) {
                    restoreDirectoryOrDelete(imagesDir, imagesBackupDir)
                }
                throw e
            }
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            dbBackupFile?.delete()
            walBackupFile?.delete()
            shmBackupFile?.delete()
            imagesBackupDir?.deleteRecursively()
            tempImportFile.delete()
            tempExtractDir.deleteRecursively()
            AppDatabase.isRestoring = false
        }
    }

    override fun getDefaultBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "process_record_backup_$timestamp.zip"
    }

    private fun addFileToZipIfExists(zip: ZipOutputStream, source: File, entryName: String) {
        if (!source.exists() || !source.isFile) return
        val entry = ZipEntry(entryName)
        zip.putNextEntry(entry)
        FileInputStream(source).use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
    }

    private fun addDirectoryToZipIfExists(zip: ZipOutputStream, sourceDir: File, entryPrefix: String) {
        if (!sourceDir.exists() || !sourceDir.isDirectory) return
        sourceDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val relative = file.relativeTo(sourceDir).invariantSeparatorsPath
                addFileToZipIfExists(zip, file, entryPrefix + relative)
            }
    }

    private fun restoreFromZipBackup(
        zipFile: File,
        extractDir: File,
        dbFile: File,
        walFile: File,
        shmFile: File,
        imagesDir: File
    ) {
        extractDir.deleteRecursively()
        extractDir.mkdirs()

        unzipToDirectory(zipFile, extractDir)

        val extractedDb = File(extractDir, ZIP_ENTRY_DB)
        if (!hasSqliteHeader(extractedDb)) {
            throw IllegalArgumentException("备份文件无效或已损坏")
        }

        extractedDb.copyTo(dbFile, overwrite = true)

        walFile.delete()
        shmFile.delete()
        File(extractDir, ZIP_ENTRY_DB_WAL).takeIf { it.exists() }?.copyTo(walFile, overwrite = true)
        File(extractDir, ZIP_ENTRY_DB_SHM).takeIf { it.exists() }?.copyTo(shmFile, overwrite = true)

        val extractedImagesDir = File(extractDir, ZIP_ENTRY_IMAGES_PREFIX.removeSuffix("/"))
        imagesDir.deleteRecursively()
        if (extractedImagesDir.exists()) {
            copyDirectory(extractedImagesDir, imagesDir)
        }
    }

    private fun unzipToDirectory(zipFile: File, destinationDir: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val outputFile = resolveZipEntryPath(destinationDir, entry.name)
                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    FileOutputStream(outputFile).use { output ->
                        zipInput.copyTo(output)
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
    }

    private fun resolveZipEntryPath(destinationDir: File, entryName: String): File {
        return resolveZipEntryPathSafe(destinationDir, entryName)
    }

    private fun backupIfExists(source: File, backupFile: File): File? {
        if (!source.exists()) return null
        backupFile.delete()
        source.copyTo(backupFile, overwrite = true)
        return backupFile
    }

    private fun backupDirectoryIfExists(sourceDir: File, backupDir: File): File? {
        if (!sourceDir.exists()) return null
        backupDir.deleteRecursively()
        copyDirectory(sourceDir, backupDir)
        return backupDir
    }

    private fun restoreFileOrDelete(target: File, backup: File?) {
        if (backup != null && backup.exists()) {
            backup.copyTo(target, overwrite = true)
        } else {
            target.delete()
        }
    }

    private fun restoreDirectoryOrDelete(targetDir: File, backupDir: File?) {
        targetDir.deleteRecursively()
        if (backupDir != null && backupDir.exists()) {
            copyDirectory(backupDir, targetDir)
        }
    }

    private fun copyDirectory(sourceDir: File, targetDir: File) {
        sourceDir.walkTopDown().forEach { source ->
            val relativePath = source.relativeTo(sourceDir).path
            val target = if (relativePath.isEmpty()) {
                targetDir
            } else {
                File(targetDir, relativePath)
            }
            if (source.isDirectory) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                source.copyTo(target, overwrite = true)
            }
        }
    }
}
