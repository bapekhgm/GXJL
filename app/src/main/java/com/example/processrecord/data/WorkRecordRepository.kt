package com.example.processrecord.data

import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.data.entity.WorkRecord
import kotlinx.coroutines.flow.Flow

interface WorkRecordRepository {
    fun getAllRecordsStream(): Flow<List<WorkRecord>>
    fun getRecordsByDateStream(date: Long): Flow<List<WorkRecord>>
    fun getRecordsByDateRangeStream(startDate: Long, endDate: Long): Flow<List<WorkRecord>>
    fun getTotalAmountByDateStream(date: Long): Flow<Double?>
    fun getTotalAmountByMonthStream(year: Int, month: Int): Flow<Double?>
    fun getStatsByStyleStream(): Flow<List<StyleStat>>
    fun getStatsByStyleForRangeStream(startDate: Long, endDate: Long): Flow<List<StyleStat>>
    /** 查询指定月份内有记录的日期（UTC 天级时间戳集合） */
    fun getRecordDatesInMonthStream(monthStart: Long, monthEnd: Long): Flow<List<Long>>
    suspend fun getRecordStream(id: Long): WorkRecord?
    /**
     * 原子性插入记录（含图片和颜色明细），任意一步失败整体回滚。
     * @return 新插入记录的 id
     */
    suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ): Long

    /**
     * 原子性更新记录（含图片和颜色明细），任意一步失败整体回滚。
     */
    suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    )

    suspend fun deleteRecord(record: WorkRecord)

    // Image handling（只读，供 ExportViewModel 等使用）
    suspend fun getImagesForRecord(recordId: Long): List<String>

    // Color details（只读，供编辑时回填使用）
    suspend fun getColorItemsForRecord(recordId: Long): List<WorkRecordColorItem>

    // Custom color presets
    fun getColorPresetsStream(): Flow<List<ColorPreset>>
    suspend fun addColorPreset(name: String, hexValue: String, groupId: Long)
    suspend fun updateColorPreset(preset: ColorPreset)
    suspend fun deleteColorPreset(preset: ColorPreset)

    // Color items batch query (for list page)
    fun getColorItemsByRecordIdsStream(recordIds: List<Long>): Flow<List<WorkRecordColorItem>>

    // Color groups
    fun getColorGroupsStream(): Flow<List<ColorGroup>>
    suspend fun addColorGroup(name: String): Long
    suspend fun updateColorGroup(group: ColorGroup)
    suspend fun deleteColorGroup(group: ColorGroup)
}
