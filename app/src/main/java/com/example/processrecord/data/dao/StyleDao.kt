package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.Flow

@Dao
interface StyleDao {
    @Query("SELECT * FROM styles ORDER BY name ASC")
    fun getAllStyles(): Flow<List<Style>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyle(style: Style)

    @Delete
    suspend fun deleteStyle(style: Style)

    @Query("DELETE FROM styles WHERE name = :name")
    suspend fun deleteStyleByName(name: String)

    @Query("SELECT * FROM styles WHERE name = :name LIMIT 1")
    suspend fun getStyleByName(name: String): Style?
}
