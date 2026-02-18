package com.example.processrecord.data.dao

import androidx.room.*
import com.example.processrecord.data.entity.WorkRecordImage

@Dao
interface WorkRecordImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: WorkRecordImage)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<WorkRecordImage>)

    @Query("SELECT * FROM work_record_images WHERE workRecordId = :workRecordId ORDER BY createTime ASC")
    suspend fun getImagesByWorkRecordId(workRecordId: Long): List<WorkRecordImage>

    @Delete
    suspend fun delete(image: WorkRecordImage)
    
    @Query("DELETE FROM work_record_images WHERE workRecordId = :workRecordId")
    suspend fun deleteByWorkRecordId(workRecordId: Long)
}
