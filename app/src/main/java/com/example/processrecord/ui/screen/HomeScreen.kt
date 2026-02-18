package com.example.processrecord.ui.screen
import com.google.accompanist.flowlayout.FlowRow

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.ExportViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordListViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordStatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToRecordEntry: () -> Unit,
    navigateToRecordEdit: (Long) -> Unit,
    navigateToRecordCopy: (Long) -> Unit, // Add copy navigation
    navigateToProcessList: () -> Unit,
    listViewModel: WorkRecordListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    statsViewModel: WorkRecordStatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    exportViewModel: ExportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val workRecordListUiState by listViewModel.workRecordListUiState.collectAsState()
    val selectedDate by listViewModel.selectedDate.collectAsState()
    
    // Calculate daily total from the current list directly
    val dailyTotal = workRecordListUiState.workRecordList.sumOf { it.amount }
    
    val monthTotal by statsViewModel.currentMonthTotalAmount.collectAsState()
    val styleStats by statsViewModel.currentMonthStatsByStyle.collectAsState() // Use Monthly Stats
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
                        TextButton(onClick = { showExportMenu = true }) {
                            Text("导出")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
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
                        }
                    }

                    TextButton(
                        onClick = navigateToProcessList,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("工序管理", style = MaterialTheme.typography.labelLarge)
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
                         Icon(Icons.Default.ArrowBack, contentDescription = "前一天")
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
                         Icon(Icons.Default.ArrowForward, contentDescription = "后一天")
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
                    imageVector = Icons.Default.List,
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

/** 解析 hex 颜色字符串，失败时返回 null */
private fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) { null }

/** 判断颜色是否偏亮，用于决定文字颜色 */
private fun Color.isLight(): Boolean {
    val r = red; val g = green; val b = blue
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.6f
}

@Composable
fun WorkRecordItem(
    record: WorkRecord,
    colorItems: List<WorkRecordColorItem> = emptyList(),
    onCopy: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    fun fmtQty(v: Double) = if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(v)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // ── 第一行：手袋图标 + 款号 + 序号 + 金额 ───────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${record.style}#",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (record.serialNumber.isNotBlank()) {
                        Text(
                            text = record.serialNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "¥ ${String.format(Locale.getDefault(), "%.2f", record.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── 第二行：工序 + 数量×单价 + 总数量 ───────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.processName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                if (record.quantity > 0) {
                    Text(
                        text = buildString {
                            append(fmtQty(record.quantity))
                            if (record.unitPrice > 0) append(" × ¥${fmtQty(record.unitPrice)}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 总数量（如果有且与 quantity 不同）
                if (record.totalQuantity > 0 && record.totalQuantity != record.quantity) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "总",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = fmtQty(record.totalQuantity),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── 第三行：颜色明细（优先用 colorItems 真实颜色）────────
            val hasColorItems = colorItems.isNotEmpty()
            val hasColorText = record.color.isNotBlank()
            if (hasColorItems || hasColorText) {
                Spacer(modifier = Modifier.height(6.dp))
                if (hasColorItems) {
                    // 使用真实颜色数据
                    FlowRow(mainAxisSpacing = 5.dp, crossAxisSpacing = 5.dp) {
                        colorItems.forEach { item ->
                            val bgColor = parseHexColor(item.colorHex)
                                ?: MaterialTheme.colorScheme.secondaryContainer
                            val textColor = if (bgColor.isLight()) Color(0xFF212121) else Color.White
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(bgColor, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = item.colorName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = textColor
                                )
                                if (item.quantity > 0) {
                                    Text(
                                        text = " · ${fmtQty(item.quantity)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = textColor.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 降级：解析文字颜色
                    val colorList = record.color.split(Regex("\\s+"))
                        .map { it.trim() }.filter { it.isNotEmpty() }
                    FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
                        colorList.forEach { colorItem ->
                            val regex = Regex("([\\u4e00-\\u9fa5A-Za-z]+)(\\d+(?:\\.\\d+)?)?")
                            val match = regex.find(colorItem)
                            val name = match?.groupValues?.get(1) ?: colorItem
                            val qty = match?.groupValues?.getOrNull(2)?.takeIf { it.isNotBlank() }
                            Text(
                                text = if (qty != null) "$name·$qty" else name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // ── 时间行 ───────────────────────────────────────────────
            if (record.startTime > 0) {
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormat2 = SimpleDateFormat("MM/dd", Locale.getDefault())
                val startDateStr = dateFormat2.format(Date(record.startTime))
                val startTimeStr = timeFormat.format(Date(record.startTime))
                val endStr = if (record.endTime > 0) {
                    val endDateStr = dateFormat2.format(Date(record.endTime))
                    val endTimeStr = timeFormat.format(Date(record.endTime))
                    // 跨天时显示日期
                    if (endDateStr != startDateStr) "$endDateStr $endTimeStr" else endTimeStr
                } else ""
                val durText = if (record.endTime > record.startTime) {
                    val ms = record.endTime - record.startTime
                    val d = ms / 86400000L
                    val h = (ms % 86400000L) / 3600000L
                    val m = (ms % 3600000L) / 60000L
                    buildString {
                        append("  ")
                        if (d > 0) append("${d}天")
                        if (h > 0) append("${h}h")
                        append("${m}m")
                    }
                } else ""
                Text(
                    text = "⏱  $startDateStr $startTimeStr" +
                        (if (endStr.isNotEmpty()) " – $endStr" else "") + durText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // ── 备注行 ───────────────────────────────────────────────
            if (record.remark.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                if (record.startTime <= 0) {
                    // 没有时间行时才加分隔线
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = "💬  ${record.remark}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── 复制按钮 ─────────────────────────────────────────────
            if (onCopy != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onCopy,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp, vertical = 2.dp
                        )
                    ) {
                        Text(
                            "复制此记录",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<StyleStat>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    val total = data.sumOf { it.totalAmount }.takeIf { it > 0.0 } ?: 1.0
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    Canvas(modifier = modifier.padding(8.dp)) {
        var startAngle = -90f
        val canvasSize = this.size
        val diameter = minOf(canvasSize.width, canvasSize.height)
        val left = (canvasSize.width - diameter) / 2f
        val top = (canvasSize.height - diameter) / 2f

        data.forEachIndexed { index, stat ->
            val sweep = ((stat.totalAmount / total) * 360.0).toFloat()
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(left, top),
                size = Size(diameter, diameter)
            )
            startAngle += sweep
        }
    }
}

@Composable
fun StyleStatsBody(
    styleStats: List<StyleStat>,
    workRecordList: List<WorkRecord>,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (styleStats.isEmpty()) {
        // Empty State Optimization
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Info, // Or a better illustration if available
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "本月暂无统计数据",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "快去记一笔吧！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items = styleStats) { stat ->
                var expanded by remember { mutableStateOf(false) }
                
                Column {
                    val styleRecordCount = workRecordList.count { it.style == stat.style }
                    ListItem(
                        headlineContent = { Text(stat.style, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            Text(
                                text = "${styleRecordCount} 条记录",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥${String.format(Locale.getDefault(), "%.2f", stat.totalAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (expanded) "收起 ▲" else "展开 ▼",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                    
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            val styleRecords = workRecordList.filter { it.style == stat.style }
                            if (styleRecords.isEmpty()) {
                                Text(
                                    text = "无记录详情",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    styleRecords.forEach { record ->
                                        WorkRecordItem(
                                            record = record,
                                            onClick = { onRecordClick(record.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

/**
 * 自定义日历选择弹窗
 * 有记录的日期显示蓝色圆点标记，当前选中日期高亮
 */
@Composable
fun RecordCalendarDialog(
    selectedDate: Long,
    calendarYear: Int,
    calendarMonth: Int,   // 0-based
    recordDates: Set<String>,  // "yyyy-MM-dd"
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onMonthChanged: (year: Int, month: Int) -> Unit
) {
    val today = java.util.Calendar.getInstance()
    val todayStr = "%04d-%02d-%02d".format(
        today.get(java.util.Calendar.YEAR),
        today.get(java.util.Calendar.MONTH) + 1,
        today.get(java.util.Calendar.DAY_OF_MONTH)
    )
    val selectedCal = java.util.Calendar.getInstance().also { it.timeInMillis = selectedDate }
    val selectedStr = "%04d-%02d-%02d".format(
        selectedCal.get(java.util.Calendar.YEAR),
        selectedCal.get(java.util.Calendar.MONTH) + 1,
        selectedCal.get(java.util.Calendar.DAY_OF_MONTH)
    )

    // 计算当月天数和第一天是星期几
    val firstDayCal = java.util.Calendar.getInstance().also {
        it.set(calendarYear, calendarMonth, 1, 0, 0, 0)
        it.set(java.util.Calendar.MILLISECOND, 0)
    }
    val daysInMonth = firstDayCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    // 星期几（1=周日，2=周一...），转为0=周一偏移
    var firstDayOfWeek = firstDayCal.get(java.util.Calendar.DAY_OF_WEEK) - 2
    if (firstDayOfWeek < 0) firstDayOfWeek = 6  // 周日 → 6

    val monthNames = listOf("1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月")
    val weekDays = listOf("一","二","三","四","五","六","日")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题行：年月 + 前后月切换
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val c = java.util.Calendar.getInstance()
                        c.set(calendarYear, calendarMonth, 1)
                        c.add(java.util.Calendar.MONTH, -1)
                        onMonthChanged(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH))
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "上月")
                    }
                    Text(
                        text = "${calendarYear}年 ${monthNames[calendarMonth]}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = {
                        val c = java.util.Calendar.getInstance()
                        c.set(calendarYear, calendarMonth, 1)
                        c.add(java.util.Calendar.MONTH, 1)
                        onMonthChanged(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH))
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "下月")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 星期标题行
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (day == "六" || day == "日")
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 日期格子
                val totalCells = firstDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val day = cellIndex - firstDayOfWeek + 1
                            if (day < 1 || day > daysInMonth) {
                                Box(modifier = Modifier.weight(1f).height(40.dp))
                            } else {
                                val dateStr = "%04d-%02d-%02d".format(calendarYear, calendarMonth + 1, day)
                                val isSelected = dateStr == selectedStr
                                val isToday = dateStr == todayStr
                                val hasRecord = dateStr in recordDates

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            val cal = java.util.Calendar.getInstance()
                                            cal.set(calendarYear, calendarMonth, day, 0, 0, 0)
                                            cal.set(java.util.Calendar.MILLISECOND, 0)
                                            onDateSelected(cal.timeInMillis)
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        // 有记录的小圆点
                                        if (hasRecord) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.primary,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 图例说明
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Text("有记录", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                        Text("今天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("关闭") }
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

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    )
}

/**
 * 服装吊牌线条图标（Canvas 自绘）
 * 形状：上方有绳子穿孔的圆角矩形标签
 */
@Composable
fun ClothingTagIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.1f
        val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // 绳子：从左上角斜线到顶部小圆孔
        val holeX = w * 0.62f
        val holeY = h * 0.22f
        val holeR = w * 0.09f

        // 绳子曲线：从左上角到圆孔
        val ropePath = Path().apply {
            moveTo(w * 0.05f, h * 0.05f)
            cubicTo(
                w * 0.15f, h * 0.0f,
                w * 0.45f, h * 0.05f,
                holeX, holeY - holeR
            )
        }
        drawPath(ropePath, color = color, style = stroke)

        // 圆孔
        drawCircle(
            color = color,
            radius = holeR,
            center = Offset(holeX, holeY),
            style = stroke
        )

        // 标签主体：圆角矩形，顶部中间有小缺口（穿绳处）
        val tagLeft = w * 0.12f
        val tagTop = h * 0.28f
        val tagRight = w * 0.95f
        val tagBottom = h * 0.97f
        val cornerR = w * 0.15f

        val tagPath = Path().apply {
            // 从顶部左侧开始，顺时针
            moveTo(tagLeft + cornerR, tagTop)
            // 顶部到圆孔左侧
            lineTo(holeX - holeR - strokeW * 0.5f, tagTop)
            // 跳过圆孔（缺口）
            moveTo(holeX + holeR + strokeW * 0.5f, tagTop)
            // 顶部右侧
            lineTo(tagRight - cornerR, tagTop)
            // 右上圆角
            quadraticBezierTo(tagRight, tagTop, tagRight, tagTop + cornerR)
            // 右边
            lineTo(tagRight, tagBottom - cornerR)
            // 右下圆角
            quadraticBezierTo(tagRight, tagBottom, tagRight - cornerR, tagBottom)
            // 底边
            lineTo(tagLeft + cornerR, tagBottom)
            // 左下圆角
            quadraticBezierTo(tagLeft, tagBottom, tagLeft, tagBottom - cornerR)
            // 左边
            lineTo(tagLeft, tagTop + cornerR)
            // 左上圆角
            quadraticBezierTo(tagLeft, tagTop, tagLeft + cornerR, tagTop)
        }
        drawPath(tagPath, color = color, style = stroke)
    }
}
