package com.example.processrecord.data.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.text.Charsets

class DatabaseBackupManagerFileValidationTest {

    @Test
    fun hasSqliteHeader_returnsTrue_forValidSqliteFileHeader() {
        val file = File.createTempFile("sqlite-valid-", ".db")
        try {
            val header = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
            file.outputStream().use { output ->
                output.write(header)
                output.write(byteArrayOf(1, 2, 3, 4))
            }
            assertTrue(hasSqliteHeader(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun hasSqliteHeader_returnsFalse_forInvalidHeader() {
        val file = File.createTempFile("sqlite-invalid-", ".db")
        try {
            file.writeText("NOT A SQLITE DATABASE FILE")
            assertFalse(hasSqliteHeader(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun hasSqliteHeader_returnsFalse_forTooShortFile() {
        val file = File.createTempFile("sqlite-short-", ".db")
        try {
            file.writeBytes(byteArrayOf(0x53, 0x51, 0x4C))
            assertFalse(hasSqliteHeader(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun resolveZipEntryPathSafe_allowsChildPath() {
        val root = Files.createTempDirectory("zip-safe-root-").toFile()
        try {
            val resolved = resolveZipEntryPathSafe(root, "images/a.jpg")
            assertTrue(resolved.path.contains("images"))
            assertTrue(resolved.path.endsWith("a.jpg"))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun resolveZipEntryPathSafe_rejectsTraversalPath() {
        val root = Files.createTempDirectory("zip-safe-root-").toFile()
        try {
            var thrown = false
            try {
                resolveZipEntryPathSafe(root, "../outside.db")
            } catch (_: IOException) {
                thrown = true
            }
            assertTrue(thrown)
        } finally {
            root.deleteRecursively()
        }
    }
}
