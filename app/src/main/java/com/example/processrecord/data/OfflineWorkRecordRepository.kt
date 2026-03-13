package com.example.processrecord.data

import com.example.processrecord.data.dao.ColorGroupDao
import com.example.processrecord.data.dao.ColorPresetDao
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.dao.WorkRecordColorItemDao
import com.example.processrecord.data.dao.WorkRecordDao
import com.example.processrecord.data.dao.WorkRecordImageDao
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.data.entity.WorkRecordImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.util.Calendar

class OfflineWorkRecordRepository(
    private val workRecordDao: WorkRecordDao,
    private val workRecordImageDao: WorkRecordImageDao,
    private val workRecordColorItemDao: WorkRecordColorItemDao,
    private val colorPresetDao: ColorPresetDao,
    private val colorGroupDao: ColorGroupDao
) : WorkRecordRepository {

    override fun getAllRecordsStream(): Flow<List<WorkRecord>> = workRecordDao.getAllRecords()

    override fun getRecordsByDateRangeStream(startDate: Long, endDate: Long): Flow<List<WorkRecord>> =
        workRecordDao.getRecordsByDateRange(startDate, endDate)

    override fun getRecordsByDateStream(date: Long): Flow<List<WorkRecord>> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.timeInMillis

        return workRecordDao.getRecordsByDateRange(startDate, endDate)
    }

    override fun getTotalAmountByDateStream(date: Long): Flow<Long?> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.timeInMillis

        return workRecordDao.getTotalAmountByDateRange(startDate, endDate)
    }

    override fun getTotalAmountByMonthStream(year: Int, month: Int): Flow<Long?> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0) // Calendar month is 0-based.
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.timeInMillis

        return workRecordDao.getTotalAmountByDateRange(startDate, endDate)
    }

    override fun getStatsByStyleStream(): Flow<List<StyleStat>> = workRecordDao.getStatsByStyle()

    override fun getStatsByStyleForRangeStream(startDate: Long, endDate: Long): Flow<List<StyleStat>> =
        workRecordDao.getStatsByStyleForRange(startDate, endDate)

    override fun getRecordDatesInMonthStream(monthStart: Long, monthEnd: Long): Flow<List<Long>> =
        workRecordDao.getRecordDatesInMonth(monthStart, monthEnd)

    override suspend fun getRecordStream(id: Long): WorkRecord? = workRecordDao.getRecordById(id)

    override suspend fun getLatestRecord(): WorkRecord? = workRecordDao.getLatestRecord()

    // Atomic insert: write record + images + color items in one transaction.
    override suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ): Long {
        val imageEntities = images.map { path -> WorkRecordImage(workRecordId = 0, imagePath = path) }
        return workRecordDao.insertRecordWithDetails(record, imageEntities, colorItems)
    }

    // Atomic update: replace record and related data in one transaction.
    override suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ) {
        val previousImagePaths = workRecordImageDao.getImagesByWorkRecordId(record.id).map { it.imagePath }
        val imageEntities = images.map { path -> WorkRecordImage(workRecordId = record.id, imagePath = path) }
        workRecordDao.updateRecordWithDetails(record, imageEntities, colorItems)

        val retainedPaths = images.toSet()
        for (path in previousImagePaths.asSequence().filterNot { it in retainedPaths }.distinct()) {
            deleteLocalImageIfUnreferenced(path)
        }
    }

    override suspend fun deleteRecord(record: WorkRecord) {
        val imagePaths = workRecordImageDao.getImagesByWorkRecordId(record.id).map { it.imagePath }
        workRecordDao.deleteRecord(record)
        for (path in imagePaths.distinct()) {
            deleteLocalImageIfUnreferenced(path)
        }
    }

    override suspend fun getImagesForRecord(recordId: Long): List<String> {
        return workRecordImageDao.getImagesByWorkRecordId(recordId).map { it.imagePath }
    }

    override suspend fun getImagesByRecordIds(recordIds: List<Long>): Map<Long, List<String>> {
        if (recordIds.isEmpty()) return emptyMap()
        return workRecordImageDao.getImagesByWorkRecordIds(recordIds)
            .groupBy { it.workRecordId }
            .mapValues { (_, images) -> images.map { it.imagePath } }
    }

    override suspend fun getColorItemsForRecord(recordId: Long): List<WorkRecordColorItem> {
        return workRecordColorItemDao.getByRecordId(recordId)
    }

    override fun getColorItemsByRecordIdsStream(recordIds: List<Long>): Flow<List<WorkRecordColorItem>> {
        if (recordIds.isEmpty()) return flowOf(emptyList())
        return workRecordColorItemDao.getByRecordIds(recordIds)
    }

    override fun getColorPresetsStream(): Flow<List<ColorPreset>> = colorPresetDao.getAllPresets()

    override suspend fun addColorPreset(name: String, hexValue: String, groupId: Long) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        val existing = colorPresetDao.getPresetByName(trimmedName)
        if (existing != null) {
            throw ColorPresetNameConflictException(trimmedName)
        }
        val sortOrder = trimmedName.hashCode().and(Int.MAX_VALUE)
        val insertedId = colorPresetDao.insertPreset(
            ColorPreset(
                name = trimmedName,
                hexValue = hexValue,
                groupId = groupId,
                sortOrder = sortOrder
            )
        )
        if (insertedId == -1L) {
            throw ColorPresetNameConflictException(trimmedName)
        }
    }

    override suspend fun updateColorPreset(preset: ColorPreset) {
        val trimmedName = preset.name.trim()
        if (trimmedName.isEmpty()) return
        val existing = colorPresetDao.getPresetByName(trimmedName)
        if (existing != null && existing.id != preset.id) {
            throw ColorPresetNameConflictException(trimmedName)
        }
        colorPresetDao.updatePreset(preset.copy(name = trimmedName))
    }

    override suspend fun deleteColorPreset(preset: ColorPreset) {
        colorPresetDao.deletePreset(preset)
    }

    override fun getColorGroupsStream(): Flow<List<ColorGroup>> = colorGroupDao.getAllGroups()

    override suspend fun addColorGroup(name: String): Long {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return 0L

        val existing = colorGroupDao.getGroupByName(trimmedName)
        if (existing != null) {
            throw ColorGroupNameConflictException(trimmedName)
        }

        val insertedId = colorGroupDao.insertGroup(
            ColorGroup(
                name = trimmedName,
                sortOrder = trimmedName.hashCode().and(Int.MAX_VALUE)
            )
        )
        if (insertedId != -1L) return insertedId
        throw ColorGroupNameConflictException(trimmedName)
    }

    override suspend fun updateColorGroup(group: ColorGroup) {
        val trimmedName = group.name.trim()
        if (trimmedName.isEmpty()) return
        val existing = colorGroupDao.getGroupByName(trimmedName)
        if (existing != null && existing.id != group.id) {
            throw ColorGroupNameConflictException(trimmedName)
        }
        colorGroupDao.updateGroup(group.copy(name = trimmedName))
    }

    override suspend fun deleteColorGroup(group: ColorGroup) {
        colorPresetDao.moveGroupPresets(group.id, 0)
        colorGroupDao.deleteGroup(group)
    }

    private suspend fun deleteLocalImageIfUnreferenced(path: String) {
        if (!path.startsWith("/")) return
        if (workRecordImageDao.countByImagePath(path) > 0) return

        runCatching {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
