package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class WorkRecordStatsViewModel(private val workRecordRepository: WorkRecordRepository) : ViewModel() {

    private val selectedDate = MutableStateFlow(System.currentTimeMillis())

    private val selectedMonth = selectedDate
        .map(::buildMonthContext)
        .distinctUntilChanged()

    private val allRecords: StateFlow<List<WorkRecord>> =
        workRecordRepository.getAllRecordsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 使用 SQL 聚合查询，避免全量加载
    // 金额已从 Double 改为 Long（分），需要转换为元显示
    val todayTotalAmount: StateFlow<Long> =
        workRecordRepository.getTotalAmountByDateStream(getTodayStartTimestamp())
            .map { it ?: 0L }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    // 月度统计跟随当前选中日期所在月份，而不是固定系统当前月。
    val currentMonthTotalAmount: StateFlow<Long> =
        selectedMonth.flatMapLatest { monthContext ->
            workRecordRepository.getTotalAmountByMonthStream(
                monthContext.year,
                monthContext.month
            )
        }.map { it ?: 0L }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )

    val currentMonthRecords: StateFlow<List<WorkRecord>> =
        selectedMonth.flatMapLatest { monthContext ->
            workRecordRepository.getRecordsByDateRangeStream(
                monthContext.startDate,
                monthContext.endDateInclusive
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 按款号统计：金额按记录汇总，总数量同款号只取一次，避免重复累计。
    val currentMonthStyleStats: StateFlow<List<StyleStat>> =
        currentMonthRecords
            .map(::buildStyleStats)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 当前选中日期所在月份的颜色明细 Map<recordId, List<ColorItem>>
    val currentMonthColorItemsMap: StateFlow<Map<Long, List<WorkRecordColorItem>>> =
        currentMonthRecords.flatMapLatest { records ->
            val ids = records.map { it.id }
            if (ids.isEmpty()) {
                flowOf(emptyMap())
            } else {
                workRecordRepository.getColorItemsByRecordIdsStream(ids).map { items ->
                    items.groupBy { it.workRecordId }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val monthlyStatsSections: StateFlow<List<MonthlyStyleStatsSection>> =
        allRecords.map(::buildMonthlyStatsSections)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val monthlyStatsColorItemsMap: StateFlow<Map<Long, List<WorkRecordColorItem>>> =
        allRecords.flatMapLatest { records ->
            val ids = records.map { it.id }.distinct()
            if (ids.isEmpty()) {
                flowOf(emptyMap())
            } else {
                workRecordRepository.getColorItemsByRecordIdsStream(ids).map { items ->
                    items.groupBy { it.workRecordId }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun updateSelectedDate(date: Long) {
        if (date > 0L) {
            selectedDate.value = date
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun buildMonthContext(date: Long): MonthContext {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (date > 0L) date else System.currentTimeMillis()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDateInclusive = calendar.timeInMillis

        return MonthContext(
            year = year,
            month = month,
            startDate = startDate,
            endDateInclusive = endDateInclusive
        )
    }

    private fun buildMonthlyStatsSections(records: List<WorkRecord>): List<MonthlyStyleStatsSection> {
        return records.groupBy { record ->
            buildMonthBucket(record.date)
        }.map { (monthBucket, monthRecords) ->
            val sortedRecords = monthRecords.sortedWith(
                compareByDescending<WorkRecord> { it.date }
                    .thenByDescending { it.createTime }
                    .thenByDescending { it.id }
            )
            val styleStats = buildStyleStats(sortedRecords)
            MonthlyStyleStatsSection(
                year = monthBucket.year,
                month = monthBucket.month,
                monthStart = monthBucket.startDate,
                totalAmount = sortedRecords.sumOf { it.amount },
                totalQuantity = styleStats.sumOf { it.totalQuantity },
                styleStats = styleStats,
                records = sortedRecords
            )
        }.sortedByDescending { it.monthStart }
    }

    private fun buildMonthBucket(date: Long): MonthBucket {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return MonthBucket(
            year = year,
            month = month,
            startDate = calendar.timeInMillis
        )
    }

    private fun buildStyleStats(records: List<WorkRecord>): List<StyleStat> {
        return records.groupBy { it.style }
            .map { (style, items) ->
                StyleStat(
                    style = style,
                    totalAmount = items.sumOf { it.amount },
                    totalQuantity = resolveStyleTotalQuantity(items)
                )
            }
            .sortedWith(
                compareByDescending<StyleStat> { it.totalAmount }
                    .thenBy { it.style }
            )
    }

    private fun resolveStyleTotalQuantity(records: List<WorkRecord>): Long {
        val sortedRecords = records.sortedWith(
            compareByDescending<WorkRecord> { it.date }
                .thenByDescending { it.createTime }
                .thenByDescending { it.id }
        )
        return sortedRecords.firstOrNull { it.totalQuantity > 0L }?.totalQuantity
            ?: sortedRecords.firstOrNull()?.totalQuantity
            ?: 0L
    }
}

private data class MonthContext(
    val year: Int,
    val month: Int,
    val startDate: Long,
    val endDateInclusive: Long
)

private data class MonthBucket(
    val year: Int,
    val month: Int,
    val startDate: Long
)

data class MonthlyStyleStatsSection(
    val year: Int,
    val month: Int,
    val monthStart: Long,
    val totalAmount: Long,
    val totalQuantity: Long,
    val styleStats: List<StyleStat>,
    val records: List<WorkRecord>
)
