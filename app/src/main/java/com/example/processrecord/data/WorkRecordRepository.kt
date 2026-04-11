package com.example.processrecord.data

import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.flow.Flow

interface WorkRecordRepository {
    fun getAllRecordsStream(): Flow<List<WorkRecord>>
    fun getRecordsByDateStream(date: Long): Flow<List<WorkRecord>>
    fun getRecordsByDateRangeStream(startDate: Long, endDate: Long): Flow<List<WorkRecord>>
    fun getRecordsByGroupIdStream(entryGroupId: String): Flow<List<WorkRecord>>
    fun getTotalAmountByDateStream(date: Long): Flow<Long?>
    fun getTotalAmountByMonthStream(year: Int, month: Int): Flow<Long?>
    fun getStatsByStyleStream(): Flow<List<StyleStat>>
    fun getStatsByStyleForRangeStream(startDate: Long, endDate: Long): Flow<List<StyleStat>>

    // Record dates in a month (UTC day-level timestamps).
    fun getRecordDatesInMonthStream(monthStart: Long, monthEnd: Long): Flow<List<Long>>

    suspend fun getRecordStream(id: Long): WorkRecord?
    suspend fun getRecordsByGroupId(entryGroupId: String): List<WorkRecord>
    suspend fun getLatestRecord(): WorkRecord?

    // Atomic write: record + images + color items in one transaction.
    suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ): Long

    suspend fun insertRecordGroupWithDetails(entries: List<WorkRecordInsertPayload>): List<Long>

    // Atomic update: replace record + related images + color items in one transaction.
    suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    )

    suspend fun deleteRecord(record: WorkRecord)

    // Image queries used by list/export/edit features.
    suspend fun getImagesForRecord(recordId: Long): List<String>
    suspend fun getImagesByRecordIds(recordIds: List<Long>): Map<Long, List<String>>

    // Color detail query used by edit page restore.
    suspend fun getColorItemsForRecord(recordId: Long): List<WorkRecordColorItem>

    // Custom color presets.
    fun getColorPresetsStream(): Flow<List<ColorPreset>>
    suspend fun addColorPreset(name: String, hexValue: String, groupId: Long)
    suspend fun updateColorPreset(preset: ColorPreset)
    suspend fun deleteColorPreset(preset: ColorPreset)

    // Color item batch query used by list page.
    fun getColorItemsByRecordIdsStream(recordIds: List<Long>): Flow<List<WorkRecordColorItem>>

    // Color groups.
    fun getColorGroupsStream(): Flow<List<ColorGroup>>
    suspend fun addColorGroup(name: String): Long
    suspend fun updateColorGroup(group: ColorGroup)
    suspend fun deleteColorGroup(group: ColorGroup)
}

data class WorkRecordInsertPayload(
    val record: WorkRecord,
    val images: List<String>,
    val colorItems: List<WorkRecordColorItem>
)
