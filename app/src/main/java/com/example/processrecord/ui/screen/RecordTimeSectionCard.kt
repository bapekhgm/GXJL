package com.example.processrecord.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import java.util.Calendar

@Composable
fun RecordTimeSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showDateTimePicker(isStartTime: Boolean) {
        val initialTime = if (isStartTime) workRecordDetails.startTime else workRecordDetails.endTime
        if (initialTime > 0) {
            calendar.timeInMillis = initialTime
        } else {
            calendar.timeInMillis = System.currentTimeMillis()
        }

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val timestamp = calendar.timeInMillis
                    if (isStartTime) {
                        onValueChange(workRecordDetails.copy(startTime = timestamp))
                    } else {
                        onValueChange(workRecordDetails.copy(endTime = timestamp))
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        DatePickerDialog(
            context,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    SectionCard {
        SectionHeader(title = stringResource(R.string.work_record_section_time_optional)) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
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
                                contentDescription = stringResource(R.string.work_record_start_now),
                                modifier = Modifier.clickable {
                                    onValueChange(workRecordDetails.copy(startTime = System.currentTimeMillis()))
                                }
                            )
                        } else {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 8.dp, end = 40.dp)
                        .clickable { showDateTimePicker(true) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = formatTime(workRecordDetails.endTime),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.work_record_label_end_time)) },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 8.dp)
                        .clickable { showDateTimePicker(false) }
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.work_record_label_duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

