package com.example.processrecord.data

import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.dao.ColorGroupDao
import com.example.processrecord.data.dao.ColorPresetDao
import com.example.processrecord.data.dao.WorkRecordDao
import com.example.processrecord.data.dao.WorkRecordColorItemDao
import com.example.processrecord.data.entity.WorkRecord
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

import com.example.processrecord.data.dao.WorkRecordImageDao
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.data.entity.WorkRecordImage

class OfflineWorkRecordRepository(
    private val workRecordDao: WorkRecordDao,
    private val workRecordImageDao: WorkRecordImageDao,
    private val workRecordColorItemDao: WorkRecordColorItemDao,
    private val colorPresetDao: ColorPresetDao,
    private val colorGroupDao: ColorGroupDao
) : WorkRecordRepository {
    override fun getAllRecordsStream(): Flow<List<WorkRecord>> = workRecordDao.getAllRecords()

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
    
    override fun getTotalAmountByDateStream(date: Long): Flow<Double?> {
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

    override fun getTotalAmountByMonthStream(year: Int, month: Int): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0) // Month is 0-based in Calendar
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.timeInMillis
        
        return workRecordDao.getTotalAmountByRange(startDate, endDate)
    }

    override fun getStatsByStyleStream(): Flow<List<StyleStat>> = workRecordDao.getStatsByStyle()

    override fun getStatsByStyleForRangeStream(startDate: Long, endDate: Long): Flow<List<StyleStat>> = workRecordDao.getStatsByStyleForRange(startDate, endDate)

    override fun getRecordDatesInMonthStream(monthStart: Long, monthEnd: Long): Flow<List<Long>> =
        workRecordDao.getRecordDatesInMonth(monthStart, monthEnd)

    override suspend fun getRecordStream(id: Long): WorkRecord? = workRecordDao.getRecordById(id)

    /**
     * 原子性插入：主记录 + 图片 + 颜色明细在同一事务中完成。
     * 任意一步失败，整个事务回滚，不会产生孤立数据。
     */
    override suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ): Long {
        val imageEntities = images.map { path -> WorkRecordImage(workRecordId = 0, imagePath = path) }
        return workRecordDao.insertRecordWithDetails(record, imageEntities, colorItems)
    }

    /**
     * 原子性更新：主记录 + 图片 + 颜色明细在同一事务中完成。
     * 先删除旧的关联数据，再插入新的，保证数据一致性。
     */
    override suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ) {
        val imageEntities = images.map { path -> WorkRecordImage(workRecordId = record.id, imagePath = path) }
        workRecordDao.updateRecordWithDetails(record, imageEntities, colorItems)
    }

    override suspend fun deleteRecord(record: WorkRecord) = workRecordDao.deleteRecord(record)

    override suspend fun getImagesForRecord(recordId: Long): List<String> {
        return workRecordImageDao.getImagesByWorkRecordId(recordId).map { it.imagePath }
    }

    override suspend fun getColorItemsForRecord(recordId: Long): List<WorkRecordColorItem> {
        return workRecordColorItemDao.getByRecordId(recordId)
    }

    override fun getColorPresetsStream(): Flow<List<ColorPreset>> = colorPresetDao.getAllPresets()

    override suspend fun addColorPreset(name: String, hexValue: String, groupId: Long) {
        val sortOrder = name.hashCode()
        colorPresetDao.insertPreset(ColorPreset(name = name, hexValue = hexValue, groupId = groupId, sortOrder = sortOrder))
    }

    override suspend fun updateColorPreset(preset: ColorPreset) {
        colorPresetDao.updatePreset(preset)
    }

    override suspend fun deleteColorPreset(preset: ColorPreset) {
        colorPresetDao.deletePreset(preset)
    }

    override fun getColorGroupsStream(): Flow<List<ColorGroup>> = colorGroupDao.getAllGroups()

    override suspend fun addColorGroup(name: String): Long {
        return colorGroupDao.insertGroup(ColorGroup(name = name.trim(), sortOrder = name.hashCode()))
    }

    override suspend fun updateColorGroup(group: ColorGroup) {
        colorGroupDao.updateGroup(group.copy(name = group.name.trim()))
    }

    override suspend fun deleteColorGroup(group: ColorGroup) {
        val defaultGroup = colorGroupDao.getGroupByName("自定义")
            ?: ColorGroup(name = "自定义", sortOrder = 9999).let { newGroup ->
                val id = colorGroupDao.insertGroup(newGroup)
                newGroup.copy(id = id)
            }
        if (group.id == defaultGroup.id) return
        colorPresetDao.moveGroupPresets(group.id, defaultGroup.id)
        colorGroupDao.deleteGroup(group)
    }
}
