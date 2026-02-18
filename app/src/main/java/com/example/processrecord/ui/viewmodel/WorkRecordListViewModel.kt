package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.dao.WorkRecordColorItemDao
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class WorkRecordListViewModel(
    private val workRecordRepository: WorkRecordRepository,
    private val workRecordColorItemDao: WorkRecordColorItemDao
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate = _selectedDate.asStateFlow()

    /** 日历当前显示的年月（用于自定义日历） */
    private val _calendarYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _calendarMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-based
    val calendarYear = _calendarYear.asStateFlow()
    val calendarMonth = _calendarMonth.asStateFlow()

    /** 当前日历月份内有记录的日期集合（本地日期字符串 "yyyy-MM-dd"） */
    val recordDatesInMonth: StateFlow<Set<String>> =
        combine(_calendarYear, _calendarMonth) { year, month ->
            val cal = Calendar.getInstance()
            cal.set(year, month, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis
            monthStart to monthEnd
        }.flatMapLatest { (start, end) ->
            workRecordRepository.getRecordDatesInMonthStream(start, end).map { timestamps ->
                timestamps.map { ts ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = ts
                    "%04d-%02d-%02d".format(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                }.toSet()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun setCalendarMonth(year: Int, month: Int) {
        _calendarYear.value = year
        _calendarMonth.value = month
    }

    /** 当日记录列表 Flow */
    private val recordsFlow = _selectedDate.flatMapLatest { date ->
        workRecordRepository.getRecordsByDateStream(date)
    }

    /** 当日记录对应的颜色明细 Map<recordId, List<ColorItem>> */
    private val colorItemsFlow = recordsFlow.flatMapLatest { records ->
        val ids = records.map { it.id }
        if (ids.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(emptyMap())
        } else {
            workRecordColorItemDao.getByRecordIds(ids).map { items ->
                items.groupBy { it.workRecordId }
            }
        }
    }

    val workRecordListUiState: StateFlow<WorkRecordListUiState> =
        combine(recordsFlow, colorItemsFlow) { records, colorMap ->
            WorkRecordListUiState(
                workRecordList = records,
                colorItemsMap = colorMap
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkRecordListUiState()
        )

    fun updateSelectedDate(date: Long) {
        _selectedDate.value = date
    }

    fun incrementDate() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _selectedDate.value
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        _selectedDate.value = calendar.timeInMillis
    }

    fun decrementDate() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _selectedDate.value
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        _selectedDate.value = calendar.timeInMillis
    }

    fun deleteRecord(record: WorkRecord) {
        viewModelScope.launch {
            workRecordRepository.deleteRecord(record)
        }
    }
}

data class WorkRecordListUiState(
    val workRecordList: List<WorkRecord> = emptyList(),
    val colorItemsMap: Map<Long, List<WorkRecordColorItem>> = emptyMap()
)
