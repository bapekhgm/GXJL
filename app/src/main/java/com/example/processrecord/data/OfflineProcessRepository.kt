package com.example.processrecord.data

import com.example.processrecord.data.dao.ProcessDao
import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.flow.Flow

class OfflineProcessRepository(private val processDao: ProcessDao) : ProcessRepository {
    override fun getAllProcessesStream(): Flow<List<Process>> = processDao.getAllProcesses()

    override suspend fun getProcessStream(id: Long): Process? = processDao.getProcessById(id)

    override suspend fun insertProcess(process: Process) = processDao.insertProcess(process)

    override suspend fun deleteProcess(process: Process) = processDao.deleteProcess(process)

    override suspend fun updateProcess(process: Process) = processDao.updateProcess(process)
}
