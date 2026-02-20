package com.example.processrecord.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.processrecord.data.dao.ProcessDao
import com.example.processrecord.data.dao.StyleDao
import com.example.processrecord.data.dao.ColorGroupDao
import com.example.processrecord.data.dao.ColorPresetDao
import com.example.processrecord.data.dao.WorkRecordDao
import com.example.processrecord.data.dao.WorkRecordColorItemDao
import com.example.processrecord.data.dao.WorkRecordImageDao
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.ColorGroup
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
    version = 8,
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

        // ---------------------------------------------------------------
        // 正式迁移脚本（保留用户数据）
        // 说明：每次升级数据库版本时，在此处添加对应的 Migration 对象。
        // 命名规范：MIGRATION_旧版本_新版本
        // ---------------------------------------------------------------

        /**
         * v1 → v2：初始建表（work_records 基础字段）
         * 注：v1 时仅有 processes 和 work_records 两张表
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 示例：v2 新增了 style 字段
                db.execSQL("ALTER TABLE work_records ADD COLUMN style TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * v2 → v3：新增 styles 表（款号历史记录）
         */
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

        /**
         * v3 → v4：work_records 新增 totalQuantity、serialNumber、color 字段
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_records ADD COLUMN totalQuantity REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE work_records ADD COLUMN serialNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE work_records ADD COLUMN color TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * v4 → v5：新增 work_record_images 表（样板图片）
         */
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

        /**
         * v5 → v6：新增 work_record_color_items 表（颜色数量明细）
         */
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

        /**
         * v6 → v7：新增 color_groups 和 color_presets 表（常用颜色预设）
         */
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

        /**
         * v7 → v8：work_records 表结构调整（processId 改为可空）
         * 注：SQLite 不支持直接修改列约束，需重建表
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建新表（processId 可空）
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
                // 迁移数据
                db.execSQL(
                    """
                    INSERT INTO work_records_new 
                    SELECT id, processId, processName, style, unitPrice, quantity, amount,
                           startTime, endTime, remark, totalQuantity, serialNumber, color, date, createTime
                    FROM work_records
                    """.trimIndent()
                )
                // 删除旧表，重命名新表
                db.execSQL("DROP TABLE work_records")
                db.execSQL("ALTER TABLE work_records_new RENAME TO work_records")
            }
        }

        // 所有迁移脚本列表（按版本顺序排列）
        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        )

        fun closeDatabase() {
            Instance?.close()
            Instance = null
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "process_record_database")
                    .addMigrations(*ALL_MIGRATIONS)
                    // 注意：fallbackToDestructiveMigration 已移除，升级时将保留用户数据。
                    // 若遇到无法处理的迁移路径（如从极旧版本升级），Room 会抛出异常而非静默清空数据。
                    // 如需在开发阶段快速重置，可临时取消下方注释：
                    // .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
