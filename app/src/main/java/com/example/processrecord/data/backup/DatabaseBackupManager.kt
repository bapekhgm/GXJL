package com.example.processrecord.data.backup

import android.content.Context
import android.net.Uri
import com.example.processrecord.data.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseBackupManager(private val context: Context) {

    private val dbName = "process_record_database"

    /**
     * 导出数据库到指定 Uri（SAF）
     */
    fun exportDatabase(destUri: Uri): Result<String> {
        return try {
            // 关闭数据库连接确保数据完整
            AppDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                return Result.failure(Exception("数据库文件不存在"))
            }

            // 同时备份 wal 和 shm 文件（如果存在先 checkpoint）
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            context.contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("无法打开输出流"))

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            Result.success("备份成功 ($timestamp)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从指定 Uri 导入数据库（SAF）
     */
    fun importDatabase(sourceUri: Uri): Result<String> {
        return try {
            // 关闭数据库连接
            AppDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(dbName)

            // 先备份当前数据库
            val backupFile = File(dbFile.path + ".bak")
            if (dbFile.exists()) {
                dbFile.copyTo(backupFile, overwrite = true)
            }

            try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return Result.failure(Exception("无法打开输入流"))

                // 删除 wal 和 shm 文件，让 Room 重新创建
                File(dbFile.path + "-wal").delete()
                File(dbFile.path + "-shm").delete()

                Result.success("恢复成功，请重启应用以加载数据")
            } catch (e: Exception) {
                // 恢复失败，还原备份
                if (backupFile.exists()) {
                    backupFile.copyTo(dbFile, overwrite = true)
                }
                throw e
            } finally {
                backupFile.delete()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDefaultBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "工序记录_备份_$timestamp.db"
    }
}