package com.example.processrecord.ui.viewmodel

import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class WorkRecordStatsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun currentMonthStats_followSelectedDateMonth() = runTest {
        val febRecord = testRecord(
            id = 1L,
            style = "A-100",
            amount = 1200L,
            quantity = 3L,
            date = dayStart(2026, 2, 10)
        )
        val marchRecord = testRecord(
            id = 2L,
            style = "B-200",
            amount = 2400L,
            quantity = 5L,
            date = dayStart(2026, 3, 5)
        )
        val repository = FakeStatsWorkRecordRepository(
            records = listOf(febRecord, marchRecord)
        )
        val viewModel = WorkRecordStatsViewModel(repository)
        val subscriptions = collectStateFlows(viewModel)

        viewModel.updateSelectedDate(febRecord.date)
        advanceUntilIdle()

        assertEquals(1200L, viewModel.currentMonthTotalAmount.value)
        assertEquals(listOf(1L), viewModel.currentMonthRecords.value.map { it.id })
        assertEquals(listOf("A-100"), viewModel.currentMonthStyleStats.value.map { it.style })

        viewModel.updateSelectedDate(marchRecord.date)
        advanceUntilIdle()

        assertEquals(2400L, viewModel.currentMonthTotalAmount.value)
        assertEquals(listOf(2L), viewModel.currentMonthRecords.value.map { it.id })
        assertEquals(listOf("B-200"), viewModel.currentMonthStyleStats.value.map { it.style })

        subscriptions.forEach(Job::cancel)
    }

    @Test
    fun currentMonthRecords_excludeNextMonthBoundaryRecord() = runTest {
        val febLastDayRecord = testRecord(
            id = 1L,
            style = "FEB",
            amount = 1000L,
            quantity = 2L,
            date = dayStart(2026, 2, 28)
        )
        val marchBoundaryRecord = testRecord(
            id = 2L,
            style = "MAR",
            amount = 2000L,
            quantity = 4L,
            date = dayStart(2026, 3, 1)
        )
        val repository = FakeStatsWorkRecordRepository(
            records = listOf(febLastDayRecord, marchBoundaryRecord)
        )
        val viewModel = WorkRecordStatsViewModel(repository)
        val subscriptions = collectStateFlows(viewModel)

        viewModel.updateSelectedDate(febLastDayRecord.date)
        advanceUntilIdle()

        assertEquals(listOf(1L), viewModel.currentMonthRecords.value.map { it.id })
        assertEquals(1000L, viewModel.currentMonthTotalAmount.value)
        assertEquals(listOf("FEB"), viewModel.currentMonthStyleStats.value.map { it.style })

        subscriptions.forEach(Job::cancel)
    }

    @Test
    fun currentMonthStats_refreshWhenSelectedMonthRecordChanges() = runTest {
        val targetDate = dayStart(2026, 2, 12)
        val repository = FakeStatsWorkRecordRepository(
            records = listOf(
                testRecord(
                    id = 1L,
                    style = "OLD",
                    amount = 1500L,
                    quantity = 3L,
                    date = targetDate
                )
            ),
            colorItems = listOf(
                WorkRecordColorItem(
                    id = 1L,
                    workRecordId = 1L,
                    colorName = "red",
                    colorHex = "#FF0000",
                    quantity = 3L,
                    deficit = 0L,
                    sortOrder = 0
                )
            )
        )
        val viewModel = WorkRecordStatsViewModel(repository)
        val subscriptions = collectStateFlows(viewModel)

        viewModel.updateSelectedDate(targetDate)
        advanceUntilIdle()

        repository.seedRecords(
            listOf(
                testRecord(
                    id = 1L,
                    style = "NEW",
                    amount = 2800L,
                    quantity = 7L,
                    date = targetDate
                )
            )
        )
        repository.seedColorItems(
            listOf(
                WorkRecordColorItem(
                    id = 2L,
                    workRecordId = 1L,
                    colorName = "blue",
                    colorHex = "#0000FF",
                    quantity = 7L,
                    deficit = 0L,
                    sortOrder = 0
                )
            )
        )
        advanceUntilIdle()

        assertEquals(2800L, viewModel.currentMonthTotalAmount.value)
        assertEquals("NEW", viewModel.currentMonthStyleStats.value.single().style)
        assertEquals(2800L, viewModel.currentMonthStyleStats.value.single().totalAmount)
        assertEquals(7L, viewModel.currentMonthColorItemsMap.value.getValue(1L).single().quantity)
        assertEquals("blue", viewModel.currentMonthColorItemsMap.value.getValue(1L).single().colorName)

        subscriptions.forEach(Job::cancel)
    }

    private fun TestScope.collectStateFlows(viewModel: WorkRecordStatsViewModel): List<Job> {
        return listOf(
            backgroundScope.launch { viewModel.currentMonthTotalAmount.collect { } },
            backgroundScope.launch { viewModel.currentMonthRecords.collect { } },
            backgroundScope.launch { viewModel.currentMonthStyleStats.collect { } },
            backgroundScope.launch { viewModel.currentMonthColorItemsMap.collect { } }
        )
    }
}

private class FakeStatsWorkRecordRepository(
    records: List<WorkRecord> = emptyList(),
    colorItems: List<WorkRecordColorItem> = emptyList()
) : WorkRecordRepository {
    private val recordsFlow = MutableStateFlow(records)
    private val colorItemsFlow = MutableStateFlow(colorItems)

    override fun getAllRecordsStream(): Flow<List<WorkRecord>> = recordsFlow

    override fun getRecordsByDateStream(date: Long): Flow<List<WorkRecord>> {
        val start = normalizeDayStart(date)
        val end = dayEnd(start)
        return recordsFlow.map { records ->
            records.filter { it.date in start..end }
        }
    }

    override fun getRecordsByDateRangeStream(startDate: Long, endDate: Long): Flow<List<WorkRecord>> =
        recordsFlow.map { records ->
            records.filter { it.date in startDate..endDate }
        }

    override fun getTotalAmountByDateStream(date: Long): Flow<Long?> =
        getRecordsByDateStream(date).map { records -> records.sumOf { it.amount } }

    override fun getTotalAmountByMonthStream(year: Int, month: Int): Flow<Long?> =
        recordsFlow.map { records ->
            records.filter { isInMonth(it.date, year, month) }.sumOf { it.amount }
        }

    override fun getStatsByStyleStream(): Flow<List<StyleStat>> =
        recordsFlow.map(::buildStyleStats)

    override fun getStatsByStyleForRangeStream(startDate: Long, endDate: Long): Flow<List<StyleStat>> =
        recordsFlow.map { records ->
            buildStyleStats(records.filter { it.date in startDate..endDate })
        }

    override fun getRecordDatesInMonthStream(monthStart: Long, monthEnd: Long): Flow<List<Long>> =
        recordsFlow.map { records ->
            records.filter { it.date in monthStart until monthEnd }.map { it.date }
        }

    override suspend fun getRecordStream(id: Long): WorkRecord? =
        recordsFlow.value.firstOrNull { it.id == id }

    override suspend fun getLatestRecord(): WorkRecord? = recordsFlow.value.maxByOrNull { it.createTime }

    override suspend fun insertRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ): Long = error("Not needed in WorkRecordStatsViewModelTest")

    override suspend fun updateRecordWithDetails(
        record: WorkRecord,
        images: List<String>,
        colorItems: List<WorkRecordColorItem>
    ) = Unit

    override suspend fun deleteRecord(record: WorkRecord) = Unit

    override suspend fun getImagesForRecord(recordId: Long): List<String> = emptyList()

    override suspend fun getImagesByRecordIds(recordIds: List<Long>): Map<Long, List<String>> = emptyMap()

    override suspend fun getColorItemsForRecord(recordId: Long): List<WorkRecordColorItem> =
        colorItemsFlow.value.filter { it.workRecordId == recordId }

    override fun getColorPresetsStream(): Flow<List<ColorPreset>> = flowOf(emptyList())

    override suspend fun addColorPreset(name: String, hexValue: String, groupId: Long) = Unit

    override suspend fun updateColorPreset(preset: ColorPreset) = Unit

    override suspend fun deleteColorPreset(preset: ColorPreset) = Unit

    override fun getColorItemsByRecordIdsStream(recordIds: List<Long>): Flow<List<WorkRecordColorItem>> =
        colorItemsFlow.map { items ->
            items.filter { it.workRecordId in recordIds }
        }

    override fun getColorGroupsStream(): Flow<List<ColorGroup>> = flowOf(emptyList())

    override suspend fun addColorGroup(name: String): Long = 0L

    override suspend fun updateColorGroup(group: ColorGroup) = Unit

    override suspend fun deleteColorGroup(group: ColorGroup) = Unit

    fun seedRecords(records: List<WorkRecord>) {
        recordsFlow.value = records
    }

    fun seedColorItems(colorItems: List<WorkRecordColorItem>) {
        colorItemsFlow.value = colorItems
    }
}

private fun testRecord(
    id: Long,
    style: String,
    amount: Long,
    quantity: Long,
    date: Long
): WorkRecord {
    return WorkRecord(
        id = id,
        processId = 1L,
        processName = "sew",
        style = style,
        unitPrice = if (quantity == 0L) 0L else amount / quantity,
        quantity = quantity,
        amount = amount,
        totalQuantity = quantity,
        date = date,
        createTime = date + id
    )
}

private fun buildStyleStats(records: List<WorkRecord>): List<StyleStat> {
    return records
        .groupBy { it.style }
        .map { (style, items) ->
            StyleStat(
                style = style,
                totalAmount = items.sumOf { it.amount },
                totalQuantity = items.sumOf { it.totalQuantity }
            )
        }
        .sortedByDescending { it.totalAmount }
}

private fun dayStart(year: Int, month: Int, day: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun normalizeDayStart(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun dayEnd(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = normalizeDayStart(timestamp)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}

private fun isInMonth(timestamp: Long, year: Int, month: Int): Boolean {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month - 1
}
