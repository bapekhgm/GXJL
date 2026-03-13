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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStyle(style: Style): Long

    @Delete
    suspend fun deleteStyle(style: Style)

    @Query("DELETE FROM styles WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))")
    suspend fun deleteStyleByName(name: String)

    @Query(
        """
        SELECT * FROM styles
        WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
        ORDER BY id ASC
        LIMIT 1
        """
    )
    suspend fun getStyleByName(name: String): Style?
}
