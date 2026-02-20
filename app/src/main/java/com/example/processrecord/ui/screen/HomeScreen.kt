package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.RecordCalendarDialog
import com.example.processrecord.ui.component.StyleStatsBody
import com.example.processrecord.ui.component.WorkRecordItem
import com.example.processrecord.ui.viewmodel.ExportViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordListViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordStatsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToRecordEntry: () -> Unit,
    navigateToRecordEdit: (Long) -> Unit,
    navigateToRecordCopy: (Long) -> Unit,
    navigateToProcessList: () -> Unit,
    navigateToStyleManage: () -> Unit = {},
    navigateToBackup: () -> Unit = {},
    listViewModel: WorkRecordListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    statsViewModel: WorkRecordStatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    exportViewModel: ExportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val workRecordListUiState by listViewModel.workRecordListUiState.collectAsState()
    val selectedDate by listViewModel.selectedDate.collectAsState()
    
    // Calculate daily total from the current list directly
    val dailyTotal = workRecordListUiState.workRecordList.sumOf { it.amount }
    
    val monthTotal by statsViewModel.currentMonthTotalAmount.collectAsState()
    val styleStats by statsViewModel.currentMonthStyleStats.collectAsState() // Use Monthly Stats
    val monthRecords by statsViewModel.currentMonthRecords.collectAsState() // Records for Monthly Stats details
    
    var selectedTab by remember { mutableStateOf(0) } // 0: Records, 1: Stats
    var isIncomeVisible by remember { mutableStateOf(false) } // 默认隐藏收入
    var showExportMenu by remember { mutableStateOf(false) }
    var exportStartDate by remember { mutableStateOf<Long?>(null) }
    var exportEndDate by remember { mutableStateOf<Long?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = selectedDate

    val createExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.ms-excel")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val result = exportViewModel.exportRecordsToExcel(
                    context = context,
                    uri = uri,
                    startDate = exportStartDate,
                    endDate = exportEndDate
                )
                val message = result.fold(
                    onSuccess = { summary ->
                        "导出成功，共 ${summary.recordCount} 条记录，合计 ¥${String.format(Locale.getDefault(), "%.2f", summary.totalAmount)}"
                    },
                    onFailure = { throwable -> "导出失败：${throwable.message ?: "未知错误"}" }
                )
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    var showCalendarDialog by remember { mutableStateOf(false) }
    val recordDatesInMonth by listViewModel.recordDatesInMonth.collectAsState()
    val calendarYear by listViewModel.calendarYear.collectAsState()
    val calendarMonth by listViewModel.calendarMonth.collectAsState()

    // 自定义日历弹窗
    CalendarDialogHost(
        showCalendarDialog = showCalendarDialog,
        selectedDate = selectedDate,
        calendarYear = calendarYear,
        calendarMonth = calendarMonth,
        recordDatesInMonth = recordDatesInMonth,
        onDismiss = { showCalendarDialog = false },
        onDateSelected = { date ->
            listViewModel.updateSelectedDate(date)
        },
        onMonthChanged = { year, month ->
            listViewModel.setCalendarMonth(year, month)
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("工序记录") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("工序管理") },
                                onClick = {
                                    showExportMenu = false
                                    navigateToProcessList()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("款号管理") },
                                onClick = {
                                    showExportMenu = false
                                    navigateToStyleManage()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导出全部") },
                                onClick = {
                                    showExportMenu = false
                                    exportStartDate = null
                                    exportEndDate = null
                                    val filename = "工序记录_全部_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xls"
                                    createExcelLauncher.launch(filename)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导出本月") },
                                onClick = {
                                    showExportMenu = false
                                    val monthCalendar = java.util.Calendar.getInstance()
                                    monthCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                    monthCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                    monthCalendar.set(java.util.Calendar.MINUTE, 0)
                                    monthCalendar.set(java.util.Calendar.SECOND, 0)
                                    monthCalendar.set(java.util.Calendar.MILLISECOND, 0)
                                    exportStartDate = monthCalendar.timeInMillis

                                    monthCalendar.add(java.util.Calendar.MONTH, 1)
                                    monthCalendar.add(java.util.Calendar.MILLISECOND, -1)
                                    exportEndDate = monthCalendar.timeInMillis

                                    val filename = "工序记录_本月_${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())}.xls"
                                    createExcelLauncher.launch(filename)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("数据备份") },
                                onClick = {
                                    showExportMenu = false
                                    navigateToBackup()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // 只在日记录 Tab 显示 FAB，避免遮挡统计数据
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = navigateToRecordEntry,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "记一笔")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Dashboard with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .clickable { isIncomeVisible = !isIncomeVisible }
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "当日收入",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isIncomeVisible) "¥${String.format(Locale.getDefault(), "%.2f", dailyTotal)}" else "****",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Month
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "本月收入",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isIncomeVisible) "¥${String.format(Locale.getDefault(), "%.2f", monthTotal)}" else "****",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("日记录") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("本月统计") })
            }

            if (selectedTab == 0) {
                 // Date Selector for Daily Records
                 Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                 ) {
                     IconButton(onClick = { listViewModel.decrementDate() }) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "前一天")
                     }

                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         OutlinedButton(onClick = { showCalendarDialog = true }) {
                             Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.size(8.dp))
                             val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                             Text(dateFormat.format(Date(selectedDate)))
                         }
                         // 今天快捷按钮
                         val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                         val selectedStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
                         if (selectedStr != todayStr) {
                             TextButton(
                                 onClick = { listViewModel.updateSelectedDate(System.currentTimeMillis()) },
                                 contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                             ) {
                                 Text("回到今天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                             }
                         }
                     }
                     
                     IconButton(onClick = { listViewModel.incrementDate() }) {
                         Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "后一天")
                     }
                 }
            
                 WorkRecordListBody(
                    workRecordList = workRecordListUiState.workRecordList,
                    colorItemsMap = workRecordListUiState.colorItemsMap,
                    onRecordClick = navigateToRecordEdit,
                    onRecordCopy = navigateToRecordCopy,
                    onSwipeLeft = { listViewModel.incrementDate() },
                    onSwipeRight = { listViewModel.decrementDate() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                StyleStatsBody(
                    styleStats = styleStats,
                    workRecordList = monthRecords, // Use month records for details
                    onRecordClick = navigateToRecordEdit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WorkRecordListBody(
    workRecordList: List<WorkRecord>,
    colorItemsMap: Map<Long, List<WorkRecordColorItem>> = emptyMap(),
    onRecordClick: (Long) -> Unit,
    onRecordCopy: (Long) -> Unit,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 累计水平拖动距离，超过阈值触发切换
    var dragAccum by remember { mutableStateOf(0f) }
    val swipeThreshold = 80f

    val swipeModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragEnd = { dragAccum = 0f },
            onDragCancel = { dragAccum = 0f },
            onHorizontalDrag = { _, dragAmount ->
                dragAccum += dragAmount
                if (dragAccum < -swipeThreshold) {
                    dragAccum = 0f
                    onSwipeLeft?.invoke()
                } else if (dragAccum > swipeThreshold) {
                    dragAccum = 0f
                    onSwipeRight?.invoke()
                }
            }
        )
    }

    if (workRecordList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().then(swipeModifier),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无记录，快去记一笔吧！",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().then(swipeModifier),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = workRecordList, key = { it.id }) { record ->
                WorkRecordItem(
                    record = record,
                    colorItems = colorItemsMap[record.id] ?: emptyList(),
                    onCopy = { onRecordCopy(record.id) },
                    onClick = { onRecordClick(record.id) }
                )
            }
        }
    }
}


// 在 HomeScreen 中调用日历弹窗（插入到 Scaffold 之前）
@Composable
private fun CalendarDialogHost(
    showCalendarDialog: Boolean,
    selectedDate: Long,
    calendarYear: Int,
    calendarMonth: Int,
    recordDatesInMonth: Set<String>,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onMonthChanged: (Int, Int) -> Unit
) {
    if (showCalendarDialog) {
        RecordCalendarDialog(
            selectedDate = selectedDate,
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            recordDates = recordDatesInMonth,
            onDismiss = onDismiss,
            onDateSelected = onDateSelected,
            onMonthChanged = onMonthChanged
        )
    }
}

