package com.example.processrecord.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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

    val firstDayCal = java.util.Calendar.getInstance().also {
        it.set(calendarYear, calendarMonth, 1, 0, 0, 0)
        it.set(java.util.Calendar.MILLISECOND, 0)
    }
    val daysInMonth = firstDayCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    var firstDayOfWeek = firstDayCal.get(java.util.Calendar.DAY_OF_WEEK) - 2
    if (firstDayOfWeek < 0) firstDayOfWeek = 6

    val monthNames = listOf("1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月")
    val weekDays = listOf("一","二","三","四","五","六","日")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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