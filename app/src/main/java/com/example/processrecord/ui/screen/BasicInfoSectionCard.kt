package com.example.processrecord.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.component.RecordCalendarDialog
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import java.util.Calendar

const val RECORD_DATE_FIELD_TEST_TAG = "record_date_field"

@Composable
fun BasicInfoSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit,
    showImages: Boolean = true,
    embedded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    var showCalendarDialog by remember { mutableStateOf(false) }
    var calendarYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }

    fun openCalendar() {
        val sourceTime = if (workRecordDetails.date > 0L) {
            workRecordDetails.date
        } else {
            System.currentTimeMillis()
        }
        calendar.timeInMillis = sourceTime
        calendarYear = calendar.get(Calendar.YEAR)
        calendarMonth = calendar.get(Calendar.MONTH)
        showCalendarDialog = true
    }

    val content: @Composable () -> Unit = {
        val calendarFieldInteractionSource = remember { MutableInteractionSource() }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatDate(workRecordDetails.date),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.work_record_label_record_date)) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = AppTextFieldShape,
                colors = appOutlinedTextFieldColors()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .testTag(RECORD_DATE_FIELD_TEST_TAG)
                    .clickable(
                        interactionSource = calendarFieldInteractionSource,
                        indication = null,
                        onClick = ::openCalendar
                    )
            )
        }

        if (showImages) {
            Spacer(modifier = Modifier.height(12.dp))

            RecordImageSectionCard(
                workRecordDetails = workRecordDetails,
                onValueChange = onValueChange
            )
        }
    }

    if (embedded) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            content()
        }
    } else {
        SectionCard(modifier = modifier, emphasized = true) {
            SectionHeader(
                title = stringResource(R.string.work_record_section_basic_info),
                emphasized = true
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            content()
        }
    }

    if (showCalendarDialog) {
        RecordCalendarDialog(
            selectedDate = if (workRecordDetails.date > 0L) {
                workRecordDetails.date
            } else {
                System.currentTimeMillis()
            },
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            recordDates = emptySet<String>(),
            onDismiss = { showCalendarDialog = false },
            onDateSelected = { selectedDate ->
                onValueChange(workRecordDetails.copy(date = selectedDate))
                showCalendarDialog = false
            },
            onMonthChanged = { year, month ->
                calendarYear = year
                calendarMonth = month
            }
        )
    }
}
