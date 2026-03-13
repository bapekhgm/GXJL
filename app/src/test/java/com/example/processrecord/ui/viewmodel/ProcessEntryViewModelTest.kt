package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessEntryViewModelTest {

    @Test
    fun saveProcess_returnsFailure_whenRequiredFieldsMissing() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest()
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )

        val result = viewModel.saveProcess()

        assertTrue(result.isFailure)
        assertEquals(0, fakeRepository.insertCalls)
        assertTrue(result.exceptionOrNull() is InvalidProcessInputException)
    }

    @Test
    fun deleteProcess_returnsFailure_whenProcessIdMissing() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest()
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )

        val result = viewModel.deleteProcess()

        assertTrue(result.isFailure)
        assertEquals(0, fakeRepository.deleteCalls)
        assertTrue(result.exceptionOrNull() is ProcessNotFoundException)
    }

    @Test
    fun saveProcess_insertsProcess_whenInputValid() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest()
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )
        viewModel.updateUiState(
            ProcessDetails(
                name = "Cut",
                defaultPrice = "1.25",
                unit = "piece"
            )
        )

        val result = viewModel.saveProcess()

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepository.insertCalls)
        assertEquals("Cut", fakeRepository.lastInsertedProcess?.name)
        assertEquals(1.25, fakeRepository.lastInsertedProcess?.defaultPrice ?: 0.0, 0.0)
    }

    @Test
    fun saveProcess_insertsProcess_withFallbacks_whenPriceAndUnitBlank() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest()
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )
        viewModel.updateUiState(
            ProcessDetails(
                name = "Pack",
                defaultPrice = "",
                unit = ""
            )
        )

        val result = viewModel.saveProcess()

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepository.insertCalls)
        assertEquals(0.0, fakeRepository.lastInsertedProcess?.defaultPrice ?: -1.0, 0.0)
        assertEquals(DEFAULT_PROCESS_UNIT, fakeRepository.lastInsertedProcess?.unit)
    }

    @Test
    fun saveProcess_returnsFailure_whenPriceIsInvalid() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest()
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )
        viewModel.updateUiState(
            ProcessDetails(
                name = "Cut",
                defaultPrice = "-1",
                unit = "piece"
            )
        )

        val result = viewModel.saveProcess()

        assertTrue(result.isFailure)
        assertEquals(0, fakeRepository.insertCalls)
    }

    @Test
    fun saveProcess_returnsFailure_whenProcessNameAlreadyExists() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest()
        fakeRepository.insertProcess(
            Process(
                name = "Cut",
                defaultPrice = 1.0,
                unit = "piece"
            )
        )
        val insertCallsBeforeSave = fakeRepository.insertCalls
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )
        viewModel.updateUiState(
            ProcessDetails(
                name = "cut",
                defaultPrice = "2.00",
                unit = "piece"
            )
        )

        val result = viewModel.saveProcess()

        assertTrue(result.isFailure)
        assertEquals(insertCallsBeforeSave, fakeRepository.insertCalls)
        assertTrue(result.exceptionOrNull() is ProcessAlreadyExistsException)
        assertEquals(
            "cut",
            (result.exceptionOrNull() as? ProcessAlreadyExistsException)?.processName
        )
    }

    @Test
    fun saveProcess_reactivatesInactiveProcess_whenNameAlreadyExists() = runBlocking {
        val fakeRepository = FakeProcessRepositoryForProcessEntryTest().apply {
            seed(
                listOf(
                    Process(
                        id = 9L,
                        name = "Cut",
                        defaultPrice = 1.0,
                        unit = "piece",
                        isActive = false
                    )
                )
            )
        }
        val viewModel = ProcessEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            processRepository = fakeRepository
        )
        viewModel.updateUiState(
            ProcessDetails(
                name = "cut",
                defaultPrice = "2.50",
                unit = "hour"
            )
        )

        val result = viewModel.saveProcess()

        assertTrue(result.isSuccess)
        assertEquals(0, fakeRepository.insertCalls)
        assertEquals(1, fakeRepository.updateCalls)
        val restored = fakeRepository.findById(9L)
        assertEquals("cut", restored?.name)
        assertEquals(2.50, restored?.defaultPrice ?: 0.0, 0.0)
        assertEquals("hour", restored?.unit)
        assertTrue(restored?.isActive == true)
    }
}

private class FakeProcessRepositoryForProcessEntryTest : ProcessRepository {
    private val processesFlow = MutableStateFlow(emptyList<Process>())

    var insertCalls: Int = 0
    var updateCalls: Int = 0
    var deleteCalls: Int = 0
    var lastInsertedProcess: Process? = null

    override fun getAllProcessesStream(): Flow<List<Process>> = processesFlow

    override suspend fun getProcessStream(id: Long): Process? {
        return processesFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun getProcessByName(name: String): Process? {
        return processesFlow.value.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }
    }

    override suspend fun insertProcess(process: Process): Long {
        insertCalls += 1
        val nextId = (processesFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val inserted = process.copy(id = nextId)
        lastInsertedProcess = inserted
        processesFlow.value = processesFlow.value + inserted
        return nextId
    }

    override suspend fun deleteProcess(process: Process) {
        deleteCalls += 1
        processesFlow.value = processesFlow.value.map { current ->
            if (current.id == process.id) current.copy(isActive = false) else current
        }
    }

    override suspend fun updateProcess(process: Process) {
        updateCalls += 1
        processesFlow.value = processesFlow.value.map { current ->
            if (current.id == process.id) process else current
        }
    }

    fun seed(processes: List<Process>) {
        processesFlow.value = processes
    }

    fun findById(id: Long): Process? = processesFlow.value.firstOrNull { it.id == id }
}
