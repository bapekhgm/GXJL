package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppDropdownMenu
import com.example.processrecord.ui.component.AppDropdownMenuItem
import com.example.processrecord.ui.component.AppFloatingButton
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.component.EmptyStateCard
import com.example.processrecord.ui.component.MonthlyStyleStatsBody
import com.example.processrecord.ui.component.RecordCalendarDialog
import com.example.processrecord.ui.component.StyleStatsSummaryCard
import com.example.processrecord.ui.component.WorkRecordGroupItem
import com.example.processrecord.ui.component.WorkRecordItemDisplayMode
import com.example.processrecord.ui.viewmodel.WorkRecordGroupSummary
import com.example.processrecord.ui.viewmodel.ExportViewModel
import com.example.processrecord.ui.viewmodel.MonthlyStyleStatsSection
import com.example.processrecord.ui.viewmodel.WorkRecordListViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordStatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val HOME_ADD_RECORD_BUTTON_TEST_TAG = "home_add_record_button"
const val HOME_SELECTED_DATE_CHIP_TEST_TAG = "home_selected_date_chip"
const val HOME_DAILY_RECORDS_TAB_TEST_TAG = "home_daily_records_tab"
const val HOME_MONTH_OVERVIEW_TAB_TEST_TAG = "home_month_overview_tab"
const val HOME_MONTH_STATS_TAB_TEST_TAG = "home_month_stats_tab"
const val HOME_MONTH_OVERVIEW_CARD_TEST_TAG = "home_month_overview_card"

private val HomeFabContentBottomPadding: Dp = 112.dp

@Composable
fun HomeScreen(
    navigateToRecordAdd: () -> Unit,
    navigateToGroupEntry: (String, Long?) -> Unit,
    navigateToRecordEdit: (Long) -> Unit,
    navigateToProcessList: () -> Unit,
    navigateToStyleManage: () -> Unit = {},
    navigateToBackup: () -> Unit = {},
    listViewModel: WorkRecordListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    statsViewModel: WorkRecordStatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    exportViewModel: ExportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val workRecordListUiState by listViewModel.workRecordListUiState.collectAsState()
    val selectedDate by listViewModel.selectedDate.collectAsState()
    val monthTotal by statsViewModel.currentMonthTotalAmount.collectAsState()
    val styleStats by statsViewModel.currentMonthStyleStats.collectAsState()
    val monthlyStatsSections by statsViewModel.monthlyStatsSections.collectAsState()
    val monthlyStatsColorItems by statsViewModel.monthlyStatsColorItemsMap.collectAsState()

    val dailyTotal = workRecordListUiState.workRecordList.sumOf { it.amount }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recordDatesInMonth by listViewModel.recordDatesInMonth.collectAsState()
    val calendarYear by listViewModel.calendarYear.collectAsState()
    val calendarMonth by listViewModel.calendarMonth.collectAsState()

    LaunchedEffect(selectedDate) {
        statsViewModel.updateSelectedDate(selectedDate)
    }

    var selectedOverviewTab by rememberSaveable { mutableIntStateOf(0) }
    var isIncomeVisible by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var exportStartDate by remember { mutableStateOf<Long?>(null) }
    var exportEndDate by remember { mutableStateOf<Long?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    fun formatCentsToYuan(cents: Long): String {
        val yuan = cents / 100.0
        return String.format(Locale.getDefault(), "%.2f", yuan)
    }

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
                        context.resources.getQuantityString(
                            R.plurals.home_export_success_message,
                            summary.recordCount,
                            summary.recordCount,
                            String.format(Locale.getDefault(), "%.2f", summary.totalAmount)
                        )
                    },
                    onFailure = { throwable ->
                        context.getString(
                            R.string.home_export_failed_message,
                            throwable.message
                                ?: context.getString(R.string.home_export_unknown_error)
                        )
                    }
                )
                withContext(Dispatchers.Main.immediate) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    CalendarDialogHost(
        showCalendarDialog = showCalendarDialog,
        selectedDate = selectedDate,
        calendarYear = calendarYear,
        calendarMonth = calendarMonth,
        recordDatesInMonth = recordDatesInMonth,
        onDismiss = { showCalendarDialog = false },
        onDateSelected = { date ->
            listViewModel.updateSelectedDate(date)
            showCalendarDialog = false
        },
        onMonthChanged = { year, month ->
            listViewModel.setCalendarMonth(year, month)
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
                actions = {
                    Box {
                        ChromeIconButton(
                            onClick = { showExportMenu = true },
                            icon = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.home_menu_content_description)
                        )
                        AppDropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            AppDropdownMenuItem(
                                text = stringResource(R.string.home_menu_process_manage),
                                leadingIcon = Icons.AutoMirrored.Filled.List,
                                onClick = {
                                    showExportMenu = false
                                    navigateToProcessList()
                                }
                            )
                            AppDropdownMenuItem(
                                text = stringResource(R.string.home_menu_style_manage),
                                leadingIcon = Icons.Default.Style,
                                onClick = {
                                    showExportMenu = false
                                    navigateToStyleManage()
                                }
                            )
                            AppDropdownMenuItem(
                                text = stringResource(R.string.home_menu_export_all),
                                leadingIcon = Icons.Default.FileDownload,
                                onClick = {
                                    showExportMenu = false
                                    exportStartDate = null
                                    exportEndDate = null
                                    val timestamp = SimpleDateFormat(
                                        "yyyyMMdd_HHmmss",
                                        Locale.getDefault()
                                    ).format(Date())
                                    val filename = context.getString(
                                        R.string.home_export_filename_all,
                                        timestamp
                                    )
                                    createExcelLauncher.launch(filename)
                                }
                            )
                            AppDropdownMenuItem(
                                text = stringResource(R.string.home_menu_export_current_month),
                                leadingIcon = Icons.Default.DateRange,
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

                                    val monthText = SimpleDateFormat(
                                        "yyyyMM",
                                        Locale.getDefault()
                                    ).format(Date())
                                    val filename = context.getString(
                                        R.string.home_export_filename_month,
                                        monthText
                                    )
                                    createExcelLauncher.launch(filename)
                                }
                            )
                            AppDropdownMenuItem(
                                text = stringResource(R.string.home_menu_backup),
                                leadingIcon = Icons.Default.Backup,
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
            AppFloatingButton(
                onClick = navigateToRecordAdd,
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.home_add_record_content_description),
                modifier = Modifier
                    .testTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
                    .semantics {
                        contentDescription = context.getString(
                            R.string.home_add_record_content_description
                        )
                    }
            )
        }
    ) { innerPadding ->
        OverviewHomeContent(
            dailyTotal = dailyTotal,
            monthTotal = monthTotal,
            isIncomeVisible = isIncomeVisible,
            onToggleVisible = { isIncomeVisible = !isIncomeVisible },
            formatCentsToYuan = ::formatCentsToYuan,
            selectedOverviewTab = selectedOverviewTab,
            onOverviewTabSelected = { selectedOverviewTab = it },
            selectedDate = selectedDate,
            onPreviousDayClick = { listViewModel.decrementDate() },
            onNextDayClick = { listViewModel.incrementDate() },
            onDateClick = { showCalendarDialog = true },
            onBackToTodayClick = { listViewModel.updateSelectedDate(System.currentTimeMillis()) },
            workRecordListUiState = workRecordListUiState,
            onGroupClick = { entryGroupId -> navigateToGroupEntry(entryGroupId, null) },
            onDailyRecordClick = { entryGroupId, recordId ->
                navigateToGroupEntry(entryGroupId, recordId)
            },
            onRecordClick = navigateToRecordEdit,
            styleStats = styleStats,
            monthlyStatsSections = monthlyStatsSections,
            monthlyStatsColorItems = monthlyStatsColorItems,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun OverviewHomeContent(
    dailyTotal: Long,
    monthTotal: Long,
    isIncomeVisible: Boolean,
    onToggleVisible: () -> Unit,
    formatCentsToYuan: (Long) -> String,
    selectedOverviewTab: Int,
    onOverviewTabSelected: (Int) -> Unit,
    selectedDate: Long,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
    onDateClick: () -> Unit,
    onBackToTodayClick: () -> Unit,
    workRecordListUiState: com.example.processrecord.ui.viewmodel.WorkRecordListUiState,
    onGroupClick: (String) -> Unit,
    onDailyRecordClick: (String, Long) -> Unit,
    onRecordClick: (Long) -> Unit,
    styleStats: List<com.example.processrecord.data.dao.StyleStat>,
    monthlyStatsSections: List<MonthlyStyleStatsSection>,
    monthlyStatsColorItems: Map<Long, List<WorkRecordColorItem>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        IncomeSummaryCard(
            dailyTotal = dailyTotal,
            monthTotal = monthTotal,
            isIncomeVisible = isIncomeVisible,
            onToggleVisible = onToggleVisible,
            formatCentsToYuan = formatCentsToYuan
        )

        HomeRecordStatsTabRow(
            selectedTab = selectedOverviewTab,
            onTabSelected = onOverviewTabSelected
        )

        when (selectedOverviewTab) {
            0 -> {
                DailyRecordDateSelector(
                    selectedDate = selectedDate,
                    onPreviousDayClick = onPreviousDayClick,
                    onNextDayClick = onNextDayClick,
                    onDateClick = onDateClick,
                    onBackToTodayClick = onBackToTodayClick
                )
                WorkRecordListBody(
                    groupedRecords = workRecordListUiState.groupedRecords,
                    colorItemsMap = workRecordListUiState.colorItemsMap,
                    onGroupClick = onGroupClick,
                    onRecordClick = onDailyRecordClick,
                    onSwipeLeft = onNextDayClick,
                    onSwipeRight = onPreviousDayClick,
                    displayMode = WorkRecordItemDisplayMode.LargeDaily,
                    modifier = Modifier.weight(1f)
                )
            }

            1 -> {
                MonthlyStyleStatsBody(
                    monthSections = monthlyStatsSections,
                    colorItemsMap = monthlyStatsColorItems,
                    onRecordClick = onRecordClick,
                    topContentPadding = 10.dp,
                    bottomContentPadding = HomeFabContentBottomPadding,
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                MonthOverviewBody(
                    styleStats = styleStats,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MonthOverviewBody(
    styleStats: List<com.example.processrecord.data.dao.StyleStat>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        StyleStatsSummaryCard(
            styleStats = styleStats,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag(HOME_MONTH_OVERVIEW_CARD_TEST_TAG)
        )
    }
}

@Composable
fun WorkRecordListBody(
    groupedRecords: List<WorkRecordGroupSummary>,
    colorItemsMap: Map<Long, List<WorkRecordColorItem>>,
    onGroupClick: (String) -> Unit,
    onRecordClick: (String, Long) -> Unit,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    displayMode: WorkRecordItemDisplayMode = WorkRecordItemDisplayMode.Standard,
    modifier: Modifier = Modifier
) {
    var dragAccum by remember { mutableStateOf(0f) }
    val swipeThreshold = 80f
    val isLargeDaily = displayMode == WorkRecordItemDisplayMode.LargeDaily

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

    if (groupedRecords.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().then(swipeModifier),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                title = stringResource(R.string.home_empty_records),
                subtitle = stringResource(R.string.home_empty_records_hint),
                icon = Icons.AutoMirrored.Filled.List,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().then(swipeModifier),
            contentPadding = if (isLargeDaily) {
                PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 92.dp)
            } else {
                PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 100.dp)
            },
            verticalArrangement = Arrangement.spacedBy(if (isLargeDaily) 8.dp else 10.dp)
        ) {
            items(items = groupedRecords, key = { it.entryGroupId }) { group ->
                WorkRecordGroupItem(
                    group = group,
                    colorItemsMap = colorItemsMap,
                    onClick = { onGroupClick(group.entryGroupId) },
                    onRecordClick = { recordId -> onRecordClick(group.entryGroupId, recordId) }
                )
            }
        }
    }
}

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
