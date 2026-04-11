package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.processrecord.data.entity.ColorPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorPresetDao {
    @Query("SELECT * FROM color_presets ORDER BY sortOrder ASC, id ASC")
    fun getAllPresets(): Flow<List<ColorPreset>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPreset(preset: ColorPreset): Long

    @Query(
        """
        SELECT * FROM color_presets
        WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
        ORDER BY id ASC
        LIMIT 1
        """
    )
    suspend fun getPresetByName(name: String): ColorPreset?

    @Query("SELECT * FROM color_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Long): ColorPreset?

    @Update
    suspend fun updatePreset(preset: ColorPreset)

    @Query("UPDATE color_presets SET groupId = :toGroupId WHERE groupId = :fromGroupId")
    suspend fun moveGroupPresets(fromGroupId: Long, toGroupId: Long)

    @Query("SELECT COUNT(*) FROM color_presets WHERE groupId = :groupId")
    suspend fun countByGroup(groupId: Long): Int

    @Delete
    suspend fun deletePreset(preset: ColorPreset)
}
