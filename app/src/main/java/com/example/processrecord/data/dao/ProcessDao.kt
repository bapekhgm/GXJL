package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessDao {
    @Query("SELECT * FROM processes WHERE isActive = 1 ORDER BY id DESC")
    fun getAllProcesses(): Flow<List<Process>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcess(process: Process)

    @Update
    suspend fun updateProcess(process: Process)

    @Delete
    suspend fun deleteProcess(process: Process)

    @Query("SELECT * FROM processes WHERE id = :id")
    suspend fun getProcessById(id: Long): Process?
}
