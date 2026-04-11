package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.component.AppTimePickerDialog
import com.example.processrecord.ui.component.RecordCalendarDialog
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import java.util.Calendar

private enum class RecordTimeFieldTarget {
    Start,
    End
}

@Composable
fun RecordTimeSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit
) {
    val calendar = Calendar.getInstance()
    val startFieldInteractionSource = remember { MutableInteractionSource() }
    val startNowInteractionSource = remember { MutableInteractionSource() }
    val endFieldInteractionSource = remember { MutableInteractionSource() }
    var pendingTarget by remember { mutableStateOf<RecordTimeFieldTarget?>(null) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }
    var calendarYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }

    fun openDateTimePicker(target: RecordTimeFieldTarget) {
        val initialTime = when (target) {
            RecordTimeFieldTarget.Start -> workRecordDetails.startTime
            RecordTimeFieldTarget.End -> workRecordDetails.endTime
        }
        calendar.timeInMillis = if (initialTime > 0L) {
            initialTime
        } else {
            System.currentTimeMillis()
        }
        pendingTarget = target
        pendingDateMillis = null
        calendarYear = calendar.get(Calendar.YEAR)
        calendarMonth = calendar.get(Calendar.MONTH)
        showCalendarDialog = true
    }

    SectionCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        SectionHeader(title = stringResource(R.string.work_record_section_time_optional)) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = formatTime(workRecordDetails.startTime),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.work_record_label_start_time)) },
                    trailingIcon = {
                        if (workRecordDetails.startTime == 0L) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.work_record_start_now)
                            )
                        } else {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTextFieldShape,
                    colors = appOutlinedTextFieldColors()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = startFieldInteractionSource,
                            indication = null
                        ) {
                            openDateTimePicker(RecordTimeFieldTarget.Start)
                        }
                )
                if (workRecordDetails.startTime == 0L) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(48.dp)
                            .clickable(
                                interactionSource = startNowInteractionSource,
                                indication = null
                            ) {
                                onValueChange(
                                    workRecordDetails.copy(startTime = System.currentTimeMillis())
                                )
                            }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = formatTime(workRecordDetails.endTime),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.work_record_label_end_time)) },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTextFieldShape,
                    colors = appOutlinedTextFieldColors()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = endFieldInteractionSource,
                            indication = null
                        ) {
                            openDateTimePicker(RecordTimeFieldTarget.End)
                        }
                )
            }
        }

        val durationParts = calculateDurationParts(
            startTime = workRecordDetails.startTime,
            endTime = workRecordDetails.endTime
        )
        if (durationParts != null) {
            val durationText = buildString {
                if (durationParts.days > 0) {
                    append(stringResource(R.string.work_record_duration_day_part, durationParts.days))
                }
                if (durationParts.hours > 0) {
                    append(stringResource(R.string.work_record_duration_hour_part, durationParts.hours))
                }
                append(stringResource(R.string.work_record_duration_minute_part, durationParts.minutes))
            }.trim()

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.work_record_label_duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    val selectedTimeMillis = when (pendingTarget) {
        RecordTimeFieldTarget.Start -> workRecordDetails.startTime
        RecordTimeFieldTarget.End -> workRecordDetails.endTime
        null -> 0L
    }

    if (showCalendarDialog && pendingTarget != null) {
        RecordCalendarDialog(
            selectedDate = if (selectedTimeMillis > 0L) selectedTimeMillis else System.currentTimeMillis(),
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            recordDates = emptySet<String>(),
            onDismiss = {
                showCalendarDialog = false
                pendingTarget = null
                pendingDateMillis = null
            },
            onDateSelected = { selectedDate ->
                pendingDateMillis = selectedDate
                showCalendarDialog = false
                showTimeDialog = true
            },
            onMonthChanged = { year, month ->
                calendarYear = year
                calendarMonth = month
            }
        )
    }

    val pickedDate = pendingDateMillis
    if (showTimeDialog && pendingTarget != null && pickedDate != null) {
        AppTimePickerDialog(
            initialTimeMillis = selectedTimeMillis,
            selectedDateMillis = pickedDate,
            onDismiss = {
                showTimeDialog = false
                pendingTarget = null
                pendingDateMillis = null
            },
            onConfirm = { hour, minute ->
                calendar.timeInMillis = pickedDate
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val timestamp = calendar.timeInMillis
                when (pendingTarget) {
                    RecordTimeFieldTarget.Start -> {
                        onValueChange(workRecordDetails.copy(startTime = timestamp))
                    }
                    RecordTimeFieldTarget.End -> {
                        onValueChange(workRecordDetails.copy(endTime = timestamp))
                    }
                    null -> Unit
                }
                showTimeDialog = false
                pendingTarget = null
                pendingDateMillis = null
            }
        )
    }
}
