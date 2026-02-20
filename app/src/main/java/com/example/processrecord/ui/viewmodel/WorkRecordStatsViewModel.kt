package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class WorkRecordStatsViewModel(private val workRecordRepository: WorkRecordRepository) : ViewModel() {

    // 使用 SQL 聚合查询，避免全量加载
    val todayTotalAmount: StateFlow<Double> =
        workRecordRepository.getTotalAmountByDateStream(getTodayStartTimestamp())
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

    val currentMonthTotalAmount: StateFlow<Double> = run {
        val cal = Calendar.getInstance()
        workRecordRepository.getTotalAmountByMonthStream(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        ).map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
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