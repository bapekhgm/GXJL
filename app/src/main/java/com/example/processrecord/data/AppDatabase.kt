package com.example.processrecord.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.processrecord.data.dao.ColorGroupDao
import com.example.processrecord.data.dao.ColorPresetDao
import com.example.processrecord.data.dao.ProcessDao
import com.example.processrecord.data.dao.StyleDao
import com.example.processrecord.data.dao.WorkRecordColorItemDao
import com.example.processrecord.data.dao.WorkRecordDao
import com.example.processrecord.data.dao.WorkRecordImageDao
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.data.entity.WorkRecordImage

@Database(
    entities = [
        Process::class,
        WorkRecord::class,
        Style::class,
        WorkRecordImage::class,
        WorkRecordColorItem::class,
        ColorPreset::class,
        ColorGroup::class
    ],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun processDao(): ProcessDao
    abstract fun workRecordDao(): WorkRecordDao
    abstract fun styleDao(): StyleDao
    abstract fun workRecordImageDao(): WorkRecordImageDao
    abstract fun workRecordColorItemDao(): WorkRecordColorItemDao
    abstract fun colorPresetDao(): ColorPresetDao
    abstract fun colorGroupDao(): ColorGroupDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // Prevent reopening database while restore is running.
        @Volatile
        var isRestoring: Boolean = false

        // Migration scripts in ascending order.

        // v1 -> v2: add style column to work_records.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_records ADD COLUMN style TEXT NOT NULL DEFAULT ''")
            }
        }

        // v2 -> v3: add styles table.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS styles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // v3 -> v4: add totalQuantity, serialNumber, color columns.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_records ADD COLUMN totalQuantity REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE work_records ADD COLUMN serialNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE work_records ADD COLUMN color TEXT NOT NULL DEFAULT ''")
            }
        }

        // v4 -> v5: add work_record_images table.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS work_record_images (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workRecordId INTEGER NOT NULL,
                        imagePath TEXT NOT NULL,
                        createTime INTEGER NOT NULL,
                        FOREIGN KEY(workRecordId) REFERENCES work_records(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_work_record_images_workRecordId ON work_record_images(workRecordId)")
            }
        }

        // v5 -> v6: add work_record_color_items table.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS work_record_color_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workRecordId INTEGER NOT NULL,
                        colorName TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        quantity REAL NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(workRecordId) REFERENCES work_records(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_work_record_color_items_workRecordId ON work_record_color_items(workRecordId)")
            }
        }

        // v6 -> v7: add color_groups and color_presets tables.
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
            }
        }

        // v7 -> v8: make processId nullable by rebuilding work_records.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS work_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        processId INTEGER,
                        processName TEXT NOT NULL,
                        style TEXT NOT NULL,
                        unitPrice REAL NOT NULL,
                        quantity REAL NOT NULL,
                        amount REAL NOT NULL,
                        startTime INTEGER NOT NULL DEFAULT 0,
                        endTime INTEGER NOT NULL DEFAULT 0,
                        remark TEXT NOT NULL DEFAULT '',
                        totalQuantity REAL NOT NULL DEFAULT 0.0,
                        serialNumber TEXT NOT NULL DEFAULT '',
                        color TEXT NOT NULL DEFAULT '',
                        date INTEGER NOT NULL,
                        createTime INTEGER NOT NULL,
                        FOREIGN KEY(processId) REFERENCES processes(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO work_records_new 
                    SELECT id, processId, processName, style, unitPrice, quantity, amount,
                           startTime, endTime, remark, totalQuantity, serialNumber, color, date, createTime
                    FROM work_records
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE work_records")
                db.execSQL("ALTER TABLE work_records_new RENAME TO work_records")
            }
        }

        // v8 -> v9: convert monetary and quantity fields to INTEGER.
        // unitPrice/amount are converted from yuan to cents by multiplying by 100.
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS work_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        processId INTEGER,
                        processName TEXT NOT NULL,
                        style TEXT NOT NULL,
                        unitPrice INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        startTime INTEGER NOT NULL DEFAULT 0,
                        endTime INTEGER NOT NULL DEFAULT 0,
                        remark TEXT NOT NULL DEFAULT '',
                        totalQuantity INTEGER NOT NULL DEFAULT 0,
                        serialNumber TEXT NOT NULL DEFAULT '',
                        color TEXT NOT NULL DEFAULT '',
                        date INTEGER NOT NULL,
                        createTime INTEGER NOT NULL,
                        FOREIGN KEY(processId) REFERENCES processes(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO work_records_new 
                    SELECT id, processId, processName, style,
                           CAST(unitPrice * 100 AS INTEGER),
                           CAST(quantity AS INTEGER),
                           CAST(amount * 100 AS INTEGER),
                           startTime, endTime, remark,
                           CAST(totalQuantity AS INTEGER),
                           serialNumber, color, date, createTime
                    FROM work_records
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE work_records")
                db.execSQL("ALTER TABLE work_records_new RENAME TO work_records")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS work_record_color_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workRecordId INTEGER NOT NULL,
                        colorName TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(workRecordId) REFERENCES work_records(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO work_record_color_items_new
                    SELECT id, workRecordId, colorName, colorHex,
                           CAST(quantity AS INTEGER), sortOrder
                    FROM work_record_color_items
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE work_record_color_items")
                db.execSQL("ALTER TABLE work_record_color_items_new RENAME TO work_record_color_items")
            }
        }

        // v9 -> v10: add deficit and colorCode fields to work_record_color_items.
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_record_color_items ADD COLUMN deficit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE work_record_color_items ADD COLUMN colorCode TEXT NOT NULL DEFAULT ''")
            }
        }

        // v10 -> v11: add indexes to frequently queried fields.
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_work_records_date ON work_records (date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_work_records_createTime ON work_records (createTime)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_work_records_processId ON work_records (processId)")
            }
        }

        // v11 -> v12: deduplicate style names then enforce unique index.
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    DELETE FROM styles
                    WHERE id NOT IN (
                        SELECT MIN(id) FROM styles GROUP BY name
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_styles_name ON styles(name)")
            }
        }

        // v12 -> v13: localize legacy process unit value from "piece" to "件".
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    UPDATE processes
                    SET unit = '件'
                    WHERE LOWER(TRIM(unit)) = 'piece'
                    """.trimIndent()
                )
            }
        }

        // v13 -> v14: deduplicate process names and enforce unique index on processes(name).
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TEMP TABLE IF NOT EXISTS process_dedupe_map (
                        oldId INTEGER NOT NULL,
                        keepId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO process_dedupe_map (oldId, keepId)
                    SELECT p.id AS oldId, k.keepId
                    FROM processes p
                    JOIN (
                        SELECT LOWER(TRIM(name)) AS normalizedName, MIN(id) AS keepId
                        FROM processes
                        GROUP BY LOWER(TRIM(name))
                    ) k ON LOWER(TRIM(p.name)) = k.normalizedName
                    WHERE p.id <> k.keepId
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE work_records
                    SET processId = (
                        SELECT keepId FROM process_dedupe_map WHERE oldId = work_records.processId
                    )
                    WHERE processId IN (SELECT oldId FROM process_dedupe_map)
                    """.trimIndent()
                )
                db.execSQL("DELETE FROM processes WHERE id IN (SELECT oldId FROM process_dedupe_map)")
                db.execSQL("UPDATE processes SET name = TRIM(name)")
                db.execSQL("DROP TABLE process_dedupe_map")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_processes_name ON processes(name)")
            }
        }

        // v14 -> v15: normalize style/process names and enforce case-insensitive unique indexes.
        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TEMP TABLE IF NOT EXISTS style_dedupe_map (
                        oldId INTEGER NOT NULL,
                        keepId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO style_dedupe_map (oldId, keepId)
                    SELECT s.id AS oldId, k.keepId
                    FROM styles s
                    JOIN (
                        SELECT LOWER(TRIM(name)) AS normalizedName, MIN(id) AS keepId
                        FROM styles
                        GROUP BY LOWER(TRIM(name))
                    ) k ON LOWER(TRIM(s.name)) = k.normalizedName
                    WHERE s.id <> k.keepId
                    """.trimIndent()
                )
                db.execSQL("DELETE FROM styles WHERE id IN (SELECT oldId FROM style_dedupe_map)")
                db.execSQL("UPDATE styles SET name = TRIM(name)")
                db.execSQL("DROP TABLE style_dedupe_map")
                db.execSQL("DROP INDEX IF EXISTS index_styles_name")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_styles_name ON styles(name COLLATE NOCASE)")

                db.execSQL(
                    """
                    CREATE TEMP TABLE IF NOT EXISTS process_dedupe_map (
                        oldId INTEGER NOT NULL,
                        keepId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO process_dedupe_map (oldId, keepId)
                    SELECT p.id AS oldId, k.keepId
                    FROM processes p
                    JOIN (
                        SELECT
                            LOWER(TRIM(name)) AS normalizedName,
                            COALESCE(
                                MIN(CASE WHEN isActive = 1 THEN id END),
                                MIN(id)
                            ) AS keepId
                        FROM processes
                        GROUP BY LOWER(TRIM(name))
                    ) k ON LOWER(TRIM(p.name)) = k.normalizedName
                    WHERE p.id <> k.keepId
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE work_records
                    SET processId = (
                        SELECT keepId FROM process_dedupe_map WHERE oldId = work_records.processId
                    )
                    WHERE processId IN (SELECT oldId FROM process_dedupe_map)
                    """.trimIndent()
                )
                db.execSQL("DELETE FROM processes WHERE id IN (SELECT oldId FROM process_dedupe_map)")
                db.execSQL("UPDATE processes SET name = TRIM(name)")
                db.execSQL("DROP TABLE process_dedupe_map")
                db.execSQL("DROP INDEX IF EXISTS index_processes_name")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_processes_name ON processes(name COLLATE NOCASE)")
            }
        }

        // v15 -> v16: normalize color group/preset names and enforce case-insensitive unique indexes.
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TEMP TABLE IF NOT EXISTS color_group_dedupe_map (
                        oldId INTEGER NOT NULL,
                        keepId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO color_group_dedupe_map (oldId, keepId)
                    SELECT g.id AS oldId, k.keepId
                    FROM color_groups g
                    JOIN (
                        SELECT LOWER(TRIM(name)) AS normalizedName, MIN(id) AS keepId
                        FROM color_groups
                        GROUP BY LOWER(TRIM(name))
                    ) k ON LOWER(TRIM(g.name)) = k.normalizedName
                    WHERE g.id <> k.keepId
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE color_presets
                    SET groupId = (
                        SELECT keepId FROM color_group_dedupe_map WHERE oldId = color_presets.groupId
                    )
                    WHERE groupId IN (SELECT oldId FROM color_group_dedupe_map)
                    """.trimIndent()
                )
                db.execSQL("DELETE FROM color_groups WHERE id IN (SELECT oldId FROM color_group_dedupe_map)")
                db.execSQL("UPDATE color_groups SET name = TRIM(name)")
                db.execSQL("DROP TABLE color_group_dedupe_map")
                db.execSQL("DROP INDEX IF EXISTS index_color_groups_name")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_color_groups_name ON color_groups(name COLLATE NOCASE)")

                db.execSQL(
                    """
                    CREATE TEMP TABLE IF NOT EXISTS color_preset_dedupe_map (
                        oldId INTEGER NOT NULL,
                        keepId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO color_preset_dedupe_map (oldId, keepId)
                    SELECT p.id AS oldId, k.keepId
                    FROM color_presets p
                    JOIN (
                        SELECT LOWER(TRIM(name)) AS normalizedName, MIN(id) AS keepId
                        FROM color_presets
                        GROUP BY LOWER(TRIM(name))
                    ) k ON LOWER(TRIM(p.name)) = k.normalizedName
                    WHERE p.id <> k.keepId
                    """.trimIndent()
                )
                db.execSQL("DELETE FROM color_presets WHERE id IN (SELECT oldId FROM color_preset_dedupe_map)")
                db.execSQL("UPDATE color_presets SET name = TRIM(name)")
                db.execSQL("DROP TABLE color_preset_dedupe_map")
                db.execSQL("DROP INDEX IF EXISTS index_color_presets_name")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_color_presets_name ON color_presets(name COLLATE NOCASE)")
            }
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16
        )

        fun closeDatabase() {
            Instance?.close()
            Instance = null
        }

        fun getDatabase(context: Context): AppDatabase {
            // Avoid opening the database while restore is in progress to prevent file lock conflicts.
            if (isRestoring) {
                throw IllegalStateException("数据库正在恢复中，请稍后再试。")
            }
            return Instance ?: synchronized(this) {
                if (isRestoring) {
                    throw IllegalStateException("数据库正在恢复中，请稍后再试。")
                }
                Room.databaseBuilder(context, AppDatabase::class.java, "process_record_database")
                    .addMigrations(*ALL_MIGRATIONS)
                    // fallbackToDestructiveMigration remains disabled to preserve user data during upgrades.
                    // If an unsupported migration path is encountered, Room throws instead of silently wiping data.
                    // Use destructive migration only as a temporary local development shortcut if absolutely needed.
                    // .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
