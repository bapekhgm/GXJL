package com.example.processrecord.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessDao {
    @Query("SELECT * FROM processes WHERE isActive = 1 ORDER BY id DESC")
    fun getAllProcesses(): Flow<List<Process>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProcess(process: Process): Long

    @Update
    suspend fun updateProcess(process: Process)

    @Query("UPDATE processes SET isActive = 0 WHERE id = :processId")
    suspend fun deactivateProcessById(processId: Long)

    @Query("SELECT * FROM processes WHERE id = :id")
    suspend fun getProcessById(id: Long): Process?

    @Query(
        """
        SELECT * FROM processes
        WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
        ORDER BY isActive DESC, id ASC
        LIMIT 1
        """
    )
    suspend fun getProcessByName(name: String): Process?
}
