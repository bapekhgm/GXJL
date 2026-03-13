package com.example.processrecord.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.processrecord.R

const val RECORD_CALENDAR_PREVIOUS_MONTH_TEST_TAG = "record_calendar_previous_month"
const val RECORD_CALENDAR_NEXT_MONTH_TEST_TAG = "record_calendar_next_month"
const val RECORD_CALENDAR_DAY_TEST_TAG_PREFIX = "record_calendar_day_"

@Composable
fun RecordCalendarDialog(
    selectedDate: Long,
    calendarYear: Int,
    calendarMonth: Int, // 0-based
    recordDates: Set<String>, // "yyyy-MM-dd"
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

    val weekDays = listOf(
        stringResource(R.string.record_calendar_week_mon),
        stringResource(R.string.record_calendar_week_tue),
        stringResource(R.string.record_calendar_week_wed),
        stringResource(R.string.record_calendar_week_thu),
        stringResource(R.string.record_calendar_week_fri),
        stringResource(R.string.record_calendar_week_sat),
        stringResource(R.string.record_calendar_week_sun)
    )

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.work_record_label_record_date),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChromeIconButton(
                    onClick = {
                        val c = java.util.Calendar.getInstance()
                        c.set(calendarYear, calendarMonth, 1)
                        c.add(java.util.Calendar.MONTH, -1)
                        onMonthChanged(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH))
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.record_calendar_previous_month),
                    modifier = Modifier.testTag(RECORD_CALENDAR_PREVIOUS_MONTH_TEST_TAG)
                )
                Text(
                    text = stringResource(
                        R.string.record_calendar_month_title,
                        calendarYear,
                        calendarMonth + 1
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                ChromeIconButton(
                    onClick = {
                        val c = java.util.Calendar.getInstance()
                        c.set(calendarYear, calendarMonth, 1)
                        c.add(java.util.Calendar.MONTH, 1)
                        onMonthChanged(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH))
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.record_calendar_next_month),
                    modifier = Modifier.testTag(RECORD_CALENDAR_NEXT_MONTH_TEST_TAG)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index >= 5) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.72f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
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
                            Box(modifier = Modifier.weight(1f).height(44.dp))
                        } else {
                            val dateStr = "%04d-%02d-%02d".format(
                                calendarYear,
                                calendarMonth + 1,
                                day
                            )
                            val isSelected = dateStr == selectedStr
                            val isToday = dateStr == todayStr
                            val hasRecord = dateStr in recordDates

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .padding(3.dp)
                                    .testTag("$RECORD_CALENDAR_DAY_TEST_TAG_PREFIX$dateStr")
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                    )
                                    .clickable {
                                        val cal = java.util.Calendar.getInstance()
                                        cal.set(calendarYear, calendarMonth, day, 0, 0, 0)
                                        cal.set(java.util.Calendar.MILLISECOND, 0)
                                        onDateSelected(cal.timeInMillis)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected || isToday) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Medium
                                            }
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
                                                    if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.primary
                                                    },
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

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalendarLegend(
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(R.string.record_calendar_has_record),
                    modifier = Modifier.weight(1f)
                )
                CalendarLegend(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    text = stringResource(R.string.record_calendar_today),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_close),
                onClick = onDismiss,
                height = 44.dp
            )
        }
    )
}

@Composable
private fun CalendarLegend(
    color: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
