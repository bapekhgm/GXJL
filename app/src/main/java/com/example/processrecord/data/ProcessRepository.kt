package com.example.processrecord.data

import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.flow.Flow

interface ProcessRepository {
    fun getAllProcessesStream(): Flow<List<Process>>
    suspend fun getProcessStream(id: Long): Process?
    suspend fun insertProcess(process: Process)
    suspend fun deleteProcess(process: Process)
    suspend fun updateProcess(process: Process)
}
