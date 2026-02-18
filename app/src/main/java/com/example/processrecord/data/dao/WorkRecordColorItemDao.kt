package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordColorItemDao {
    @Query("SELECT * FROM work_record_color_items WHERE workRecordId = :recordId ORDER BY sortOrder ASC, id ASC")
    suspend fun getByRecordId(recordId: Long): List<WorkRecordColorItem>

    /** 监听一批记录的颜色明细变化（用于列表页实时更新） */
    @Query("SELECT * FROM work_record_color_items WHERE workRecordId IN (:recordIds) ORDER BY workRecordId ASC, sortOrder ASC, id ASC")
    fun getByRecordIds(recordIds: List<Long>): Flow<List<WorkRecordColorItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WorkRecordColorItem>)

    @Query("DELETE FROM work_record_color_items WHERE workRecordId = :recordId")
    suspend fun deleteByRecordId(recordId: Long)
}
