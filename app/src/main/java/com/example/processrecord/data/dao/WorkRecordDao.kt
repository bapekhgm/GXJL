package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.data.entity.WorkRecordImage
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordDao {
    @Query("SELECT * FROM work_records WHERE date >= :startDate AND date <= :endDate ORDER BY createTime DESC")
    fun getRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkRecord>>

    @Query("SELECT * FROM work_records ORDER BY createTime DESC")
    fun getAllRecords(): Flow<List<WorkRecord>>

    @Query("SELECT SUM(amount) FROM work_records WHERE date >= :startDate AND date <= :endDate")
    fun getTotalAmountByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM work_records WHERE date >= :startDate AND date <= :endDate")
    fun getTotalAmountByRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT style, SUM(amount) as totalAmount FROM work_records GROUP BY style ORDER BY totalAmount DESC")
    fun getStatsByStyle(): Flow<List<StyleStat>>

    @Query("SELECT style, SUM(amount) as totalAmount FROM work_records WHERE date >= :startDate AND date <= :endDate GROUP BY style ORDER BY totalAmount DESC")
    fun getStatsByStyleForRange(startDate: Long, endDate: Long): Flow<List<StyleStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WorkRecord): Long

    @Update
    suspend fun updateRecord(record: WorkRecord)

    @Delete
    suspend fun deleteRecord(record: WorkRecord)

    @Query("SELECT * FROM work_records WHERE id = :id")
    suspend fun getRecordById(id: Long): WorkRecord?

    /** 查询指定月份内有记录的日期（返回每天0点时间戳列表） */
    @Query("""
        SELECT DISTINCT 
            (date / 86400000) * 86400000 
        FROM work_records 
        WHERE date >= :monthStart AND date < :monthEnd
    """)
    fun getRecordDatesInMonth(monthStart: Long, monthEnd: Long): Flow<List<Long>>

    // ---------------------------------------------------------------
    // 事务方法：保证主记录 + 图片 + 颜色明细 原子性写入
    // ---------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<WorkRecordImage>)

    @Query("DELETE FROM work_record_images WHERE workRecordId = :recordId")
    suspend fun deleteImagesByRecordId(recordId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColorItems(items: List<WorkRecordColorItem>)

    @Query("DELETE FROM work_record_color_items WHERE workRecordId = :recordId")
    suspend fun deleteColorItemsByRecordId(recordId: Long)

    /**
     * 新增记录事务：原子性插入主记录 + 图片 + 颜色明细。
     * 任意一步失败，整个事务回滚，不会产生孤立数据。
     */
    @Transaction
    suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<WorkRecordImage>,
        colorItems: List<WorkRecordColorItem>
    ): Long {
        val recordId = insertRecord(record)
        if (images.isNotEmpty()) {
            insertImages(images.map { it.copy(workRecordId = recordId) })
        }
        if (colorItems.isNotEmpty()) {
            insertColorItems(colorItems.mapIndexed { index, item ->
                item.copy(id = 0, workRecordId = recordId, sortOrder = index)
            })
        }
        return recordId
    }

    /**
     * 更新记录事务：原子性更新主记录 + 替换图片 + 替换颜色明细。
     * 先删除旧的关联数据，再插入新的，保证数据一致性。
     */
    @Transaction
    suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<WorkRecordImage>,
        colorItems: List<WorkRecordColorItem>
    ) {
        updateRecord(record)
        // 替换图片
        deleteImagesByRecordId(record.id)
        if (images.isNotEmpty()) {
            insertImages(images.map { it.copy(workRecordId = record.id) })
        }
        // 替换颜色明细
        deleteColorItemsByRecordId(record.id)
        if (colorItems.isNotEmpty()) {
            insertColorItems(colorItems.mapIndexed { index, item ->
                item.copy(id = 0, workRecordId = record.id, sortOrder = index)
            })
        }
    }
}

data class StyleStat(
    val style: String,
    val totalAmount: Double
)
