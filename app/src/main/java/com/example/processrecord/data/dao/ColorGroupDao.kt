package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.processrecord.data.entity.ColorGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorGroupDao {
    @Query("SELECT * FROM color_groups ORDER BY sortOrder ASC, id ASC")
    fun getAllGroups(): Flow<List<ColorGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ColorGroup): Long

    @Update
    suspend fun updateGroup(group: ColorGroup)

    @Delete
    suspend fun deleteGroup(group: ColorGroup)

    @Query("SELECT * FROM color_groups WHERE name = :name LIMIT 1")
    suspend fun getGroupByName(name: String): ColorGroup?
}
