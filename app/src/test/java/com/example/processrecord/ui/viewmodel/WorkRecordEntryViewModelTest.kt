package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.StyleRepository
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.dao.StyleDao
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkRecordEntryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onColorEntriesChanged_withQuantities_updatesSummaryAndQuantity() {
        val viewModel = createViewModel()
        viewModel.updateUiState(
            WorkRecordDetails(
                processName = "process",
                style = "style",
                quantity = "0",
                date = 1L
            )
        )

        viewModel.onColorEntriesChanged(
            listOf(
                ColorEntryUi(colorName = "red", colorHex = "#FF0000", quantity = "2"),
                ColorEntryUi(colorName = "blue", colorHex = "#0000FF", quantity = "5")
            )
        )

        val details = viewModel.workRecordUiState.workRecordDetails
        assertEquals("7", details.quantity)
        assertEquals("red2 blue5", details.color)
    }

    @Test
    fun onColorEntriesChanged_withoutAnyQuantity_keepsManualQuantity() {
        val viewModel = createViewModel()
        viewModel.updateUiState(
            WorkRecordDetails(
                processName = "process",
                style = "style",
                quantity = "9",
                date = 1L
            )
        )

        viewModel.onColorEntriesChanged(
            listOf(
                ColorEntryUi(colorName = "red", colorHex = "#FF0000", quantity = ""),
                ColorEntryUi(colorName = "blue", colorHex = "#0000FF", quantity = "")
            )
        )

        val details = viewModel.workRecordUiState.workRecordDetails
        assertEquals("9", details.quantity)
        assertEquals("red blue", details.color)
    }

    @Test
    fun saveWorkRecord_insertsRecordAndColorItems() = runBlocking {
        val fakeWorkRecordRepository = FakeWorkRecordRepository()
        val fakeProcessRepository = FakeProcessRepository()
        val fakeStyleDao = FakeStyleDao()
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = fakeProcessRepository,
            styleRepository = StyleRepository(fakeStyleDao)
        )

        viewModel.updateUiState(
            WorkRecordDetails(
                processName = "process",
                style = "  style-A  ",
                unitPrice = "1.50",
                quantity = "2",
                imagePaths = listOf("img-1.jpg", "img-2.jpg"),
                colorEntries = listOf(
                    ColorEntryUi(
                        colorName = "red",
                        colorHex = "#FF0000",
                        quantity = "3",
                        deficit = "1",
                        colorCode = "A1"
                    )
                ),
                date = 1_700_000_000_000L
            )
        )

        val saveResult = viewModel.saveWorkRecord()
        assertTrue(saveResult.isSuccess)

        assertEquals(1, fakeWorkRecordRepository.insertCalls)
        assertNotNull(fakeWorkRecordRepository.lastInsertedRecord)
        assertNotNull(fakeWorkRecordRepository.lastInsertedColorItems)

        val savedRecord = fakeWorkRecordRepository.lastInsertedRecord!!
        assertEquals("process", savedRecord.processName)
        assertEquals("style-A", savedRecord.style)
        assertEquals(150L, savedRecord.unitPrice)
        assertEquals(300L, savedRecord.amount)

        val savedColorItem = fakeWorkRecordRepository.lastInsertedColorItems!!.single()
        assertEquals("red", savedColorItem.colorName)
        assertEquals("#FF0000", savedColorItem.colorHex)
        assertEquals(3L, savedColorItem.quantity)
        assertEquals(1L, savedColorItem.deficit)
        assertEquals("A1", savedColorItem.colorCode)
        assertEquals(0, savedColorItem.sortOrder)

        assertEquals(listOf("img-1.jpg", "img-2.jpg"), fakeWorkRecordRepository.lastInsertedImages)
        assertEquals(listOf("style-A"), fakeStyleDao.insertedStyles.map { it.name })
    }

    @Test
    fun saveWorkRecord_doesNotInsertStyle_whenSameStyleExistsIgnoringCase() = runBlocking {
        val fakeWorkRecordRepository = FakeWorkRecordRepository()
        val fakeStyleDao = FakeStyleDao(
            initialStyles = listOf(Style(id = 1L, name = "style-a"))
        )
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(fakeStyleDao)
        )

        viewModel.updateUiState(
            WorkRecordDetails(
                processName = "process",
                style = "  STYLE-A  ",
                unitPrice = "1.00",
                quantity = "2",
                date = 1L
            )
        )

        val saveResult = viewModel.saveWorkRecord()
        assertTrue(saveResult.isSuccess)
        assertEquals(0, fakeStyleDao.insertedStyles.size)
    }

    @Test
    fun saveWorkRecord_returnsFailure_whenRequiredFieldsMissing() = runBlocking {
        val fakeWorkRecordRepository = FakeWorkRecordRepository()
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )

        val result = viewModel.saveWorkRecord()

        assertTrue(result.isFailure)
        assertEquals(0, fakeWorkRecordRepository.insertCalls)
    }

    @Test
    fun saveWorkRecord_returnsFailure_whenQuantityOrUnitPriceInvalid() = runBlocking {
        val fakeWorkRecordRepository = FakeWorkRecordRepository()
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
        viewModel.updateUiState(
            WorkRecordDetails(
                processName = "process",
                style = "style",
                unitPrice = "abc",
                quantity = "0",
                date = 1L
            )
        )

        val result = viewModel.saveWorkRecord()

        assertTrue(result.isFailure)
        assertEquals(0, fakeWorkRecordRepository.insertCalls)
    }

    @Test
    fun deleteRecord_returnsFailure_whenRecordIdMissing() = runBlocking {
        val viewModel = createViewModel()

        val result = viewModel.deleteRecord()

        assertTrue(result.isFailure)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addProcess_selectsNewlyAddedProcessInUiState() = runTest {
        val viewModel = createViewModel()

        viewModel.addProcess(name = "Cut", defaultPrice = 1.25, unit = "piece")
        advanceUntilIdle()

        val details = viewModel.workRecordUiState.workRecordDetails
        assertEquals("Cut", details.processName)
        assertEquals("1.25", details.unitPrice)
        assertTrue((details.processId ?: 0L) > 0L)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addProcess_selectsExistingProcess_whenNameAlreadyExists() = runTest {
        val fakeProcessRepository = FakeProcessRepository().apply {
            seed(
                listOf(
                    Process(
                        id = 7L,
                        name = "Cut",
                        defaultPrice = 3.0,
                        unit = "piece"
                    )
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = FakeWorkRecordRepository(),
            processRepository = fakeProcessRepository,
            styleRepository = StyleRepository(FakeStyleDao())
        )

        viewModel.addProcess(name = "cut", defaultPrice = 1.25, unit = "piece")
        advanceUntilIdle()

        val details = viewModel.workRecordUiState.workRecordDetails
        assertEquals(7L, details.processId)
        assertEquals("Cut", details.processName)
        assertEquals("3.0", details.unitPrice)
        assertEquals(0, fakeProcessRepository.insertCalls)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addProcess_reactivatesExistingInactiveProcess_whenNameAlreadyExists() = runTest {
        val fakeProcessRepository = FakeProcessRepository().apply {
            seed(
                listOf(
                    Process(
                        id = 9L,
                        name = "Cut",
                        defaultPrice = 3.0,
                        unit = "piece",
                        isActive = false
                    )
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = FakeWorkRecordRepository(),
            processRepository = fakeProcessRepository,
            styleRepository = StyleRepository(FakeStyleDao())
        )

        viewModel.addProcess(name = "Cut", defaultPrice = 1.25, unit = "hour")
        advanceUntilIdle()

        val details = viewModel.workRecordUiState.workRecordDetails
        assertEquals(9L, details.processId)
        assertEquals("Cut", details.processName)
        assertEquals("1.25", details.unitPrice)
        assertEquals(0, fakeProcessRepository.insertCalls)
        assertEquals(1, fakeProcessRepository.updateCalls)
        assertTrue(fakeProcessRepository.findById(9L)?.isActive == true)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addProcess_setsError_whenInputInvalid() = runTest {
        val viewModel = createViewModel()

        viewModel.addProcess(name = "   ", defaultPrice = -1.0, unit = "")
        advanceUntilIdle()

        assertTrue(viewModel.processOperationError is InvalidProcessInputException)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun updateProcess_setsDuplicateError_whenNameAlreadyExists() = runTest {
        val fakeProcessRepository = FakeProcessRepository().apply {
            seed(
                listOf(
                    Process(id = 1L, name = "Cut", defaultPrice = 1.0, unit = "piece"),
                    Process(id = 2L, name = "Pack", defaultPrice = 2.0, unit = "piece")
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = FakeWorkRecordRepository(),
            processRepository = fakeProcessRepository,
            styleRepository = StyleRepository(FakeStyleDao())
        )

        viewModel.updateProcess(Process(id = 2L, name = "cut", defaultPrice = 2.0, unit = "piece"))
        advanceUntilIdle()

        assertTrue(viewModel.processOperationError is ProcessAlreadyExistsException)
        assertEquals(0, fakeProcessRepository.updateCalls)
    }

    @Test
    fun addCustomColorPreset_setsError_whenNameBlank() {
        val viewModel = createViewModel()

        val accepted = viewModel.addCustomColorPreset(name = "   ", hexValue = "#FF0000", groupId = 1L)

        assertEquals(false, accepted)
        assertTrue(viewModel.colorManageOperationError is InvalidColorPresetInputException)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addCustomColorPreset_emitsSuccessNotice_whenInsertSucceeds() = runTest {
        val fakeWorkRecordRepository = FakeWorkRecordRepository().apply {
            seedColorGroups(listOf(ColorGroup(id = 1L, name = "Default")))
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
        advanceUntilIdle()

        val accepted = viewModel.addCustomColorPreset(name = "Red", hexValue = "#FF0000", groupId = 1L)
        advanceUntilIdle()

        assertEquals(true, accepted)
        assertTrue(viewModel.colorManageOperationNotice is ColorManageOperationNotice.PresetAdded)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addCustomColorPreset_setsDuplicateError_whenNameAlreadyExists() = runTest {
        val fakeWorkRecordRepository = FakeWorkRecordRepository().apply {
            seedColorPresets(
                listOf(
                    ColorPreset(id = 1L, name = "Red", hexValue = "#FF0000", groupId = 1L, sortOrder = 1)
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
        advanceUntilIdle()

        val accepted = viewModel.addCustomColorPreset(name = " red ", hexValue = "#00FF00", groupId = 1L)
        advanceUntilIdle()

        assertEquals(false, accepted)
        assertTrue(viewModel.colorManageOperationError is ColorPresetAlreadyExistsException)
        assertEquals(0, fakeWorkRecordRepository.addColorPresetCalls)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun updateColorPreset_setsDuplicateError_whenTargetNameExists() = runTest {
        val fakeWorkRecordRepository = FakeWorkRecordRepository().apply {
            seedColorPresets(
                listOf(
                    ColorPreset(id = 1L, name = "Red", hexValue = "#FF0000", groupId = 1L, sortOrder = 1),
                    ColorPreset(id = 2L, name = "Blue", hexValue = "#0000FF", groupId = 1L, sortOrder = 2)
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
        advanceUntilIdle()

        val accepted = viewModel.updateColorPreset(
            ColorPreset(id = 2L, name = " red ", hexValue = "#0000FF", groupId = 1L, sortOrder = 2)
        )
        advanceUntilIdle()

        assertEquals(false, accepted)
        assertTrue(viewModel.colorManageOperationError is ColorPresetAlreadyExistsException)
        assertEquals(0, fakeWorkRecordRepository.updateColorPresetCalls)
    }

    @Test
    fun addColorGroup_setsError_whenNameBlank() {
        val viewModel = createViewModel()

        val accepted = viewModel.addColorGroup(" ")

        assertEquals(false, accepted)
        assertTrue(viewModel.colorManageOperationError is InvalidColorGroupInputException)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addColorGroup_emitsSuccessNotice_whenInsertSucceeds() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val accepted = viewModel.addColorGroup("Warm")
        advanceUntilIdle()

        assertEquals(true, accepted)
        assertTrue(viewModel.colorManageOperationNotice is ColorManageOperationNotice.GroupAdded)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addColorGroup_setsDuplicateError_whenNameAlreadyExists() = runTest {
        val fakeWorkRecordRepository = FakeWorkRecordRepository().apply {
            seedColorGroups(
                listOf(
                    ColorGroup(id = 1L, name = "Warm", sortOrder = 1)
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
        advanceUntilIdle()

        val accepted = viewModel.addColorGroup(" warm ")
        advanceUntilIdle()

        assertEquals(false, accepted)
        assertTrue(viewModel.colorManageOperationError is ColorGroupAlreadyExistsException)
        assertEquals(0, fakeWorkRecordRepository.addColorGroupCalls)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun updateColorGroup_setsDuplicateError_whenTargetNameExists() = runTest {
        val fakeWorkRecordRepository = FakeWorkRecordRepository().apply {
            seedColorGroups(
                listOf(
                    ColorGroup(id = 1L, name = "Warm", sortOrder = 1),
                    ColorGroup(id = 2L, name = "Cool", sortOrder = 2)
                )
            )
        }
        val viewModel = WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = fakeWorkRecordRepository,
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
        advanceUntilIdle()

        val accepted = viewModel.updateColorGroup(ColorGroup(id = 2L, name = " warm ", sortOrder = 2))
        advanceUntilIdle()

        assertEquals(false, accepted)
        assertTrue(viewModel.colorManageOperationError is ColorGroupAlreadyExistsException)
        assertEquals(0, fakeWorkRecordRepository.updateColorGroupCalls)
    }

    @Test
    fun consumeColorManageOperationError_clearsErrorState() {
        val viewModel = createViewModel()
        viewModel.addColorGroup(" ")

        assertTrue(viewModel.colorManageOperationError is InvalidColorGroupInputException)
        viewModel.consumeColorManageOperationError()
        assertEquals(null, viewModel.colorManageOperationError)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun consumeColorManageOperationNotice_clearsNoticeState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.addColorGroup("Warm")
        advanceUntilIdle()

        assertTrue(viewModel.colorManageOperationNotice is ColorManageOperationNotice.GroupAdded)
        viewModel.consumeColorManageOperationNotice()
        assertEquals(null, viewModel.colorManageOperationNotice)
    }

    private fun createViewModel(): WorkRecordEntryViewModel {
        return WorkRecordEntryViewModel(
            savedStateHandle = SavedStateHandle(),
            workRecordRepository = FakeWorkRecordRepository(),
            processRepository = FakeProcessRepository(),
            styleRepository = StyleRepository(FakeStyleDao())
        )
    }
}

private class FakeProcessRepository : ProcessRepository {
    private val processFlow = MutableStateFlow(emptyList<Process>())
    var insertCalls: Int = 0
    var updateCalls: Int = 0

    override fun getAllProcessesStream(): Flow<List<Process>> = processFlow.map { list ->
        list.filter { it.isActive }
    }

    override suspend fun getProcessStream(id: Long): Process? {
        return processFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun getProcessByName(name: String): Process? {
        return processFlow.value.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }
    }

    override suspend fun insertProcess(process: Process): Long {
        insertCalls += 1
        val nextId = (processFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val inserted = process.copy(id = nextId, isActive = true)
        processFlow.value = processFlow.value + inserted
        return nextId
    }

    override suspend fun deleteProcess(process: Process) {
        processFlow.value = processFlow.value.map { current ->
            if (current.id == process.id) current.copy(isActive = false) else current
        }
    }

    override suspend fun updateProcess(process: Process) {
        updateCalls += 1
        processFlow.value = processFlow.value.map { if (it.id == process.id) process else it }
    }

    fun seed(processes: List<Process>) {
        processFlow.value = processes
    }

    fun findById(id: Long): Process? = processFlow.value.firstOrNull { it.id == id }
}

private class FakeStyleDao(
    initialStyles: List<Style> = emptyList()
) : StyleDao {
    private val stylesFlow = MutableStateFlow(initialStyles)
    val insertedStyles = mutableListOf<Style>()
    private var nextId = (initialStyles.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun getAllStyles(): Flow<List<Style>> = stylesFlow

    override suspend fun insertStyle(style: Style): Long {
        if (stylesFlow.value.any {
                it.name.trim().equals(style.name.trim(), ignoreCase = true)
            }
        ) return -1L
        val insertedStyle = style.copy(id = nextId++)
        insertedStyles += insertedStyle
        stylesFlow.value = stylesFlow.value + insertedStyle
        return insertedStyle.id
    }

    override suspend fun deleteStyle(style: Style) {
        stylesFlow.value = stylesFlow.value.filterNot { it.id == style.id }
    }

    override suspend fun deleteStyleByName(name: String) {
        stylesFlow.value = stylesFlow.value.filterNot {
            it.name.trim().equals(name.trim(), ignoreCase = true)
        }
    }

    override suspend fun getStyleByName(name: String): Style? {
        return stylesFlow.value.firstOrNull {
            it.name.trim().equals(name.trim(), ignoreCase = true)
        }
    }
}

private class FakeWorkRecordRepository : WorkRecordRepository {
    val records = mutableListOf<WorkRecord>()
    var insertCalls: Int = 0
    var lastInsertedRecord: WorkRecord? = null
    var lastInsertedImages: List<String> = emptyList()
    var lastInsertedColorItems: List<WorkRecordColorItem>? = null

    private val colorPresetsFlow = MutableStateFlow(emptyList<ColorPreset>())
    private val colorGroupsFlow = MutableStateFlow(emptyList<ColorGroup>())
    var addColorPresetCalls: Int = 0
    var updateColorPresetCalls: Int = 0
    var addColorGroupCalls: Int = 0
    var updateColorGroupCalls: Int = 0

    override fun getAllRecordsStream(): Flow<List<WorkRecord>> = flowOf(records.toList())

    override fun getRecordsByDateStream(date: Long): Flow<List<WorkRecord>> =
        flowOf(records.filter { it.date == date })

    override fun getRecordsByDateRangeStream(startDate: Long, endDate: Long): Flow<List<WorkRecord>> =
        flowOf(records.filter { it.date in startDate..endDate })

    override fun getTotalAmountByDateStream(date: Long): Flow<Long?> =
        flowOf(records.filter { it.date == date }.sumOf { it.amount })

    override fun getTotalAmountByMonthStream(year: Int, month: Int): Flow<Long?> = flowOf(0L)

    override fun getStatsByStyleStream(): Flow<List<StyleStat>> = flowOf(emptyList())

    override fun getStatsByStyleForRangeStream(startDate: Long, endDate: Long): Flow<List<StyleStat>> =
        flowOf(emptyList())

    override fun getRecordDatesInMonthStream(monthStart: Long, monthEnd: Long): Flow<List<Long>> = flowOf(emptyList())

    override suspend fun getRecordStream(id: Long): WorkRecord? = records.firstOrNull { it.id == id }

    override suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ): Long {
        insertCalls += 1
        val newId = (records.maxOfOrNull { it.id } ?: 0L) + 1L
        lastInsertedRecord = record.copy(id = newId)
        lastInsertedImages = images
        lastInsertedColorItems = colorItems
        records += lastInsertedRecord!!
        return newId
    }

    override suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ) {
        records.replaceAll { if (it.id == record.id) record else it }
        lastInsertedRecord = record
        lastInsertedImages = images
        lastInsertedColorItems = colorItems
    }

    override suspend fun deleteRecord(record: WorkRecord) {
        records.removeAll { it.id == record.id }
    }

    override suspend fun getImagesForRecord(recordId: Long): List<String> = emptyList()

    override suspend fun getImagesByRecordIds(recordIds: List<Long>): Map<Long, List<String>> = emptyMap()

    override suspend fun getColorItemsForRecord(recordId: Long): List<WorkRecordColorItem> = emptyList()

    override fun getColorPresetsStream(): Flow<List<ColorPreset>> = colorPresetsFlow

    override suspend fun addColorPreset(name: String, hexValue: String, groupId: Long) {
        addColorPresetCalls += 1
        val nextId = (colorPresetsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        colorPresetsFlow.value = colorPresetsFlow.value + ColorPreset(
            id = nextId,
            name = name,
            hexValue = hexValue,
            groupId = groupId
        )
    }

    override suspend fun updateColorPreset(preset: ColorPreset) {
        updateColorPresetCalls += 1
        colorPresetsFlow.value = colorPresetsFlow.value.map {
            if (it.id == preset.id) preset else it
        }
    }

    override suspend fun deleteColorPreset(preset: ColorPreset) = Unit

    override fun getColorItemsByRecordIdsStream(recordIds: List<Long>): Flow<List<WorkRecordColorItem>> = flowOf(emptyList())

    override fun getColorGroupsStream(): Flow<List<ColorGroup>> = colorGroupsFlow

    override suspend fun addColorGroup(name: String): Long {
        addColorGroupCalls += 1
        val newId = (colorGroupsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        colorGroupsFlow.value = colorGroupsFlow.value + ColorGroup(id = newId, name = name)
        return newId
    }

    override suspend fun updateColorGroup(group: ColorGroup) {
        updateColorGroupCalls += 1
        colorGroupsFlow.value = colorGroupsFlow.value.map { if (it.id == group.id) group else it }
    }

    override suspend fun deleteColorGroup(group: ColorGroup) {
        colorGroupsFlow.value = colorGroupsFlow.value.filterNot { it.id == group.id }
    }

    fun seedColorPresets(presets: List<ColorPreset>) {
        colorPresetsFlow.value = presets
    }

    fun seedColorGroups(groups: List<ColorGroup>) {
        colorGroupsFlow.value = groups
    }
}
