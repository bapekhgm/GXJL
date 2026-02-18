package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class WorkRecordStatsViewModel(private val workRecordRepository: WorkRecordRepository) : ViewModel() {
    
    // 实时查询今天的数据，不依赖静态时间戳
    val todayTotalAmount: StateFlow<Double> = 
        workRecordRepository.getAllRecordsStream().map { records ->
            val todayStart = getTodayStartTimestamp()
            val todayEnd = todayStart + 24 * 60 * 60 * 1000
            records.filter { it.date in todayStart until todayEnd }.sumOf { it.amount }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

    // 实时查询本月数据
    val currentMonthTotalAmount: StateFlow<Double> =
        workRecordRepository.getAllRecordsStream().map { records ->
            val (monthStart, monthEnd) = getCurrentMonthRange()
            records.filter { it.date in monthStart until monthEnd }.sumOf { it.amount }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )
            
    val statsByStyle: StateFlow<List<StyleStat>> =
        workRecordRepository.getStatsByStyleStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 本月款号统计
    val currentMonthStatsByStyle: StateFlow<List<StyleStat>> =
        workRecordRepository.getAllRecordsStream().map { _ ->
            val (start, end) = getCurrentMonthRange()
            start to end
        }.flatMapLatest { (start, end) ->
            workRecordRepository.getStatsByStyleForRangeStream(start, end)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 本月所有记录 (用于统计详情)
    val currentMonthRecords: StateFlow<List<WorkRecord>> =
        workRecordRepository.getAllRecordsStream().map { records ->
            val (start, end) = getCurrentMonthRange()
            records.filter { it.date in start until end }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
