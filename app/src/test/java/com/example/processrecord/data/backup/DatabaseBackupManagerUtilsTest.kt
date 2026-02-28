package com.example.processrecord.data.backup

import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.text.Charsets
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseBackupManagerUtilsTest {

    @Test
    fun hasSqliteHeader_returnsFalseWhenFileDoesNotExist() {
        val file = File("build/test-output/not-exist-${System.nanoTime()}.db")
        assertFalse(hasSqliteHeader(file))
    }

    @Test
    fun hasSqliteHeader_returnsFalseWhenFileIsTooShort() {
        val file = Files.createTempFile("sqlite-short-", ".db").toFile()
        file.writeBytes(byteArrayOf(0x01, 0x02, 0x03))
        file.deleteOnExit()

        assertFalse(hasSqliteHeader(file))
    }

    @Test
    fun hasSqliteHeader_returnsFalseWhenHeaderIsInvalid() {
        val file = Files.createTempFile("sqlite-invalid-", ".db").toFile()
        file.writeBytes("Not a sqlite db".toByteArray(Charsets.US_ASCII))
        file.deleteOnExit()

        assertFalse(hasSqliteHeader(file))
    }

    @Test
    fun hasSqliteHeader_returnsTrueWhenHeaderIsValid() {
        val file = Files.createTempFile("sqlite-valid-", ".db").toFile()
        val validHeader = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
        val extra = ByteArray(32) { 0x20 }
        file.writeBytes(validHeader + extra)
        file.deleteOnExit()

        assertTrue(hasSqliteHeader(file))
    }

    @Test
    fun hasZipHeader_returnsTrueWhenFileIsZip() {
        val file = Files.createTempFile("backup-valid-", ".zip").toFile()
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("test.txt"))
            zip.write("ok".toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        file.deleteOnExit()

        assertTrue(hasZipHeader(file))
    }

    @Test
    fun hasZipHeader_returnsFalseWhenFileIsNotZip() {
        val file = Files.createTempFile("backup-invalid-", ".tmp").toFile()
        file.writeText("not zip")
        file.deleteOnExit()

        assertFalse(hasZipHeader(file))
    }
}
