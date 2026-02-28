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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class WorkRecordStatsViewModel(private val workRecordRepository: WorkRecordRepository) : ViewModel() {

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

    val currentMonthTotalAmount: StateFlow<Long> = run {
        val cal = Calendar.getInstance()
        workRecordRepository.getTotalAmountByMonthStream(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        ).map { it ?: 0L }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0L
            )
    }

    val currentMonthRecords: StateFlow<List<WorkRecord>> = run {
        val (start, end) = getCurrentMonthRange()
        workRecordRepository.getRecordsByDateRangeStream(start, end)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // 按款号统计：使用 SQL 聚合
    val currentMonthStyleStats: StateFlow<List<StyleStat>> = run {
        val (start, end) = getCurrentMonthRange()
        workRecordRepository.getStatsByStyleForRangeStream(start, end)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // 本月所有记录的颜色明细 Map<recordId, List<ColorItem>>
    val currentMonthColorItemsMap: StateFlow<Map<Long, List<WorkRecordColorItem>>> = run {
        val (start, end) = getCurrentMonthRange()
        workRecordRepository.getRecordsByDateRangeStream(start, end)
            .flatMapLatest { records ->
                val ids = records.map { it.id }
                if (ids.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    workRecordRepository.getColorItemsByRecordIdsStream(ids).map { items ->
                        items.groupBy { it.workRecordId }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }
}