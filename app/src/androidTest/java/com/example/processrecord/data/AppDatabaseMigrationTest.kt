package com.example.processrecord.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @Test
    fun migration11To12_removesDuplicateStyles_andEnforcesUniqueIndex() {
        val dbName = "migration-11-12-test.db"
        val migration = AppDatabase.ALL_MIGRATIONS.first {
            it.startVersion == 11 && it.endVersion == 12
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        val opened = openDatabase(context, dbName)
        val db = opened.database
        try {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS styles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("INSERT INTO styles (id, name) VALUES (1, 'S-001')")
            db.execSQL("INSERT INTO styles (id, name) VALUES (2, 'S-001')")
            db.execSQL("INSERT INTO styles (id, name) VALUES (3, 'S-002')")

            migration.migrate(db)

            val remaining = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, name FROM styles ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    remaining += cursor.getLong(0) to cursor.getString(1)
                }
            }
            assertEquals(listOf(1L to "S-001", 3L to "S-002"), remaining)

            var hasUniqueIndex = false
            db.query("PRAGMA index_list('styles')").use { cursor ->
                while (cursor.moveToNext()) {
                    val indexName = cursor.getString(1)
                    val isUnique = cursor.getInt(2) == 1
                    if (indexName == "index_styles_name" && isUnique) {
                        hasUniqueIndex = true
                    }
                }
            }
            assertTrue(hasUniqueIndex)

            var uniqueConstraintViolated = false
            try {
                db.execSQL("INSERT INTO styles (name) VALUES ('S-001')")
            } catch (_: Throwable) {
                uniqueConstraintViolated = true
            }
            assertTrue(uniqueConstraintViolated)
        } finally {
            opened.helper.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun migration12To13_convertsLegacyPieceUnitToChinese() {
        val dbName = "migration-12-13-test.db"
        val migration = AppDatabase.ALL_MIGRATIONS.first {
            it.startVersion == 12 && it.endVersion == 13
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        val opened = openDatabase(context, dbName, version = 12)
        val db = opened.database
        try {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS processes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    defaultPrice REAL NOT NULL,
                    unit TEXT NOT NULL,
                    isActive INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (1, 'P1', 1.0, 'piece', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (2, 'P2', 1.0, 'Piece', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (3, 'P3', 1.0, 'pcs', 1)")

            migration.migrate(db)

            val units = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, unit FROM processes ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    units += cursor.getLong(0) to cursor.getString(1)
                }
            }
            assertEquals(
                listOf(
                    1L to "件",
                    2L to "件",
                    3L to "pcs"
                ),
                units
            )
        } finally {
            opened.helper.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun migration13To14_deduplicatesProcesses_andRemapsWorkRecordProcessId() {
        val dbName = "migration-13-14-test.db"
        val migration = AppDatabase.ALL_MIGRATIONS.first {
            it.startVersion == 13 && it.endVersion == 14
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        val opened = openDatabase(context, dbName, version = 13)
        val db = opened.database
        try {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS processes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    defaultPrice REAL NOT NULL,
                    unit TEXT NOT NULL,
                    isActive INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS work_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    processId INTEGER
                )
                """.trimIndent()
            )

            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (1, 'Cut', 1.0, '件', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (2, 'Cut', 2.0, '件', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (3, 'Pack', 1.0, '件', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (4, ' cut ', 3.0, '件', 1)")
            db.execSQL("INSERT INTO work_records (id, processId) VALUES (1, 2)")
            db.execSQL("INSERT INTO work_records (id, processId) VALUES (2, 3)")
            db.execSQL("INSERT INTO work_records (id, processId) VALUES (3, 4)")

            migration.migrate(db)

            val processes = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, name FROM processes ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    processes += cursor.getLong(0) to cursor.getString(1)
                }
            }
            assertEquals(listOf(1L to "Cut", 3L to "Pack"), processes)

            val recordProcessIds = mutableListOf<Pair<Long, Long?>>()
            db.query("SELECT id, processId FROM work_records ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    val processId = if (cursor.isNull(1)) null else cursor.getLong(1)
                    recordProcessIds += cursor.getLong(0) to processId
                }
            }
            assertEquals(listOf(1L to 1L, 2L to 3L, 3L to 1L), recordProcessIds)

            var hasUniqueIndex = false
            db.query("PRAGMA index_list('processes')").use { cursor ->
                while (cursor.moveToNext()) {
                    val indexName = cursor.getString(1)
                    val isUnique = cursor.getInt(2) == 1
                    if (indexName == "index_processes_name" && isUnique) {
                        hasUniqueIndex = true
                    }
                }
            }
            assertTrue(hasUniqueIndex)
        } finally {
            opened.helper.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun migration14To15_normalizesNames_andEnforcesCaseInsensitiveUniqueIndexes() {
        val dbName = "migration-14-15-test.db"
        val migration = AppDatabase.ALL_MIGRATIONS.first {
            it.startVersion == 14 && it.endVersion == 15
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        val opened = openDatabase(context, dbName, version = 14)
        val db = opened.database
        try {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS styles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_styles_name ON styles(name)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS processes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    defaultPrice REAL NOT NULL,
                    unit TEXT NOT NULL,
                    isActive INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_processes_name ON processes(name)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS work_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    processId INTEGER
                )
                """.trimIndent()
            )

            db.execSQL("INSERT INTO styles (id, name) VALUES (1, 'S-001')")
            db.execSQL("INSERT INTO styles (id, name) VALUES (2, ' s-001 ')")
            db.execSQL("INSERT INTO styles (id, name) VALUES (3, 'S-002')")

            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (1, 'Cut', 1.0, 'piece', 0)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (2, ' cut ', 2.0, 'piece', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (3, 'PACK', 1.0, 'piece', 1)")
            db.execSQL("INSERT INTO processes (id, name, defaultPrice, unit, isActive) VALUES (4, 'pack', 1.0, 'piece', 0)")
            db.execSQL("INSERT INTO work_records (id, processId) VALUES (1, 1)")
            db.execSQL("INSERT INTO work_records (id, processId) VALUES (2, 4)")

            migration.migrate(db)

            val styles = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, name FROM styles ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    styles += cursor.getLong(0) to cursor.getString(1)
                }
            }
            assertEquals(listOf(1L to "S-001", 3L to "S-002"), styles)

            val processes = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, name FROM processes ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    processes += cursor.getLong(0) to cursor.getString(1)
                }
            }
            assertEquals(listOf(2L to "cut", 3L to "PACK"), processes)

            val recordProcessIds = mutableListOf<Pair<Long, Long?>>()
            db.query("SELECT id, processId FROM work_records ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    val processId = if (cursor.isNull(1)) null else cursor.getLong(1)
                    recordProcessIds += cursor.getLong(0) to processId
                }
            }
            assertEquals(listOf(1L to 2L, 2L to 3L), recordProcessIds)

            var styleDuplicateRejected = false
            try {
                db.execSQL("INSERT INTO styles (name) VALUES ('s-001')")
            } catch (_: Throwable) {
                styleDuplicateRejected = true
            }
            assertTrue(styleDuplicateRejected)

            var processDuplicateRejected = false
            try {
                db.execSQL("INSERT INTO processes (name, defaultPrice, unit, isActive) VALUES ('CUT', 1.0, 'piece', 1)")
            } catch (_: Throwable) {
                processDuplicateRejected = true
            }
            assertTrue(processDuplicateRejected)
        } finally {
            opened.helper.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun migration15To16_normalizesColorNames_andEnforcesCaseInsensitiveUniqueIndexes() {
        val dbName = "migration-15-16-test.db"
        val migration = AppDatabase.ALL_MIGRATIONS.first {
            it.startVersion == 15 && it.endVersion == 16
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        val opened = openDatabase(context, dbName, version = 15)
        val db = opened.database
        try {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS color_groups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    sortOrder INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_color_groups_name ON color_groups(name)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS color_presets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    hexValue TEXT NOT NULL,
                    groupId INTEGER NOT NULL DEFAULT 0,
                    sortOrder INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_color_presets_name ON color_presets(name)")

            db.execSQL("INSERT INTO color_groups (id, name, sortOrder) VALUES (1, 'Warm', 1)")
            db.execSQL("INSERT INTO color_groups (id, name, sortOrder) VALUES (2, ' warm ', 2)")
            db.execSQL("INSERT INTO color_groups (id, name, sortOrder) VALUES (3, 'Cool', 3)")

            db.execSQL("INSERT INTO color_presets (id, name, hexValue, groupId, sortOrder) VALUES (1, 'Red', '#FF0000', 2, 1)")
            db.execSQL("INSERT INTO color_presets (id, name, hexValue, groupId, sortOrder) VALUES (2, ' red ', '#EE0000', 1, 2)")
            db.execSQL("INSERT INTO color_presets (id, name, hexValue, groupId, sortOrder) VALUES (3, 'Blue', '#0000FF', 3, 3)")

            migration.migrate(db)

            val groups = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, name FROM color_groups ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    groups += cursor.getLong(0) to cursor.getString(1)
                }
            }
            assertEquals(listOf(1L to "Warm", 3L to "Cool"), groups)

            val presets = mutableListOf<Triple<Long, String, Long>>()
            db.query("SELECT id, name, groupId FROM color_presets ORDER BY id ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    presets += Triple(cursor.getLong(0), cursor.getString(1), cursor.getLong(2))
                }
            }
            assertEquals(
                listOf(
                    Triple(1L, "Red", 1L),
                    Triple(3L, "Blue", 3L)
                ),
                presets
            )

            var groupDuplicateRejected = false
            try {
                db.execSQL("INSERT INTO color_groups (name, sortOrder) VALUES ('WARM', 10)")
            } catch (_: Throwable) {
                groupDuplicateRejected = true
            }
            assertTrue(groupDuplicateRejected)

            var presetDuplicateRejected = false
            try {
                db.execSQL("INSERT INTO color_presets (name, hexValue, groupId, sortOrder) VALUES ('RED', '#AA0000', 1, 10)")
            } catch (_: Throwable) {
                presetDuplicateRejected = true
            }
            assertTrue(presetDuplicateRejected)
        } finally {
            opened.helper.close()
            context.deleteDatabase(dbName)
        }
    }

    private fun openDatabase(context: Context, dbName: String, version: Int = 11): OpenedDatabase {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(version) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                    }
                )
                .build()
        )
        return OpenedDatabase(helper, helper.writableDatabase)
    }

    private data class OpenedDatabase(
        val helper: SupportSQLiteOpenHelper,
        val database: SupportSQLiteDatabase
    )
}
