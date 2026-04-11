package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import com.example.processrecord.ui.viewmodel.WorkRecordFieldError

const val QUANTITY_INPUT_TEST_TAG = "quantity_input"
const val UNIT_PRICE_INPUT_TEST_TAG = "unit_price_input"

@Composable
fun RecordAmountSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit,
    quantityError: WorkRecordFieldError? = null,
    unitPriceError: WorkRecordFieldError? = null,
    embedded: Boolean = false,
    modifier: Modifier = Modifier
) {
    @Composable
    fun QuantityField(fieldModifier: Modifier) {
        OutlinedTextField(
            value = workRecordDetails.quantity,
            onValueChange = { input ->
                val filtered = input.filter(Char::isDigit)
                if (filtered.isEmpty() || filtered.toLongOrNull() != null) {
                    onValueChange(workRecordDetails.copy(quantity = filtered))
                }
            },
            label = { Text(stringResource(R.string.work_record_label_quantity)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = fieldModifier.testTag(QUANTITY_INPUT_TEST_TAG),
            singleLine = true,
            shape = AppTextFieldShape,
            colors = appOutlinedTextFieldColors(),
            isError = quantityError != null,
            supportingText = {
                workRecordFieldErrorText(quantityError)?.let { Text(text = it) }
            },
            placeholder = {
                Text(
                    text = "0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        )
    }

    @Composable
    fun UnitPriceField(fieldModifier: Modifier) {
        OutlinedTextField(
            value = workRecordDetails.unitPrice,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' }
                val dotCount = filtered.count { it == '.' }
                if (dotCount <= 1) {
                    val parts = filtered.split('.')
                    if (parts.size <= 2 && (parts.size == 1 || parts[1].length <= 2)) {
                        onValueChange(workRecordDetails.copy(unitPrice = filtered))
                    }
                }
            },
            label = { Text(stringResource(R.string.work_record_label_unit_price)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            modifier = fieldModifier.testTag(UNIT_PRICE_INPUT_TEST_TAG),
            singleLine = true,
            shape = AppTextFieldShape,
            colors = appOutlinedTextFieldColors(),
            isError = unitPriceError != null,
            supportingText = {
                workRecordFieldErrorText(unitPriceError)?.let { Text(text = it) }
            },
            placeholder = {
                Text(
                    text = "0.00",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        )
    }

    @Composable
    fun AmountFields() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuantityField(fieldModifier = Modifier.weight(1f))
            UnitPriceField(fieldModifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        )
                    ),
                    shape = MaterialTheme.shapes.small
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.work_record_label_total_amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.work_record_value_amount, workRecordDetails.amount),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (embedded) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            AmountFields()
        }
    } else {
        SectionCard(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.work_record_section_quantity_amount)) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            AmountFields()
        }
    }
}

@Composable
fun RecordRemarkSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit
) {
    SectionCard(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        SectionHeader(title = stringResource(R.string.work_record_section_remark_optional)) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        OutlinedTextField(
            value = workRecordDetails.remark,
            onValueChange = { onValueChange(workRecordDetails.copy(remark = it)) },
            label = { Text(stringResource(R.string.work_record_label_remark)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            shape = AppTextFieldShape,
            colors = appOutlinedTextFieldColors()
        )
    }
}

@Composable
fun RecordExtraFieldsSectionCard(
    workRecordDetails: WorkRecordDetails,
    onValueChange: (WorkRecordDetails) -> Unit
) {
    SectionCard(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        SectionHeader(title = stringResource(R.string.work_record_section_extra_optional)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = workRecordDetails.totalQuantity,
                    onValueChange = {
                        val filtered = it.filter(Char::isDigit)
                        if (filtered.isEmpty() || filtered.toLongOrNull() != null) {
                            onValueChange(workRecordDetails.copy(totalQuantity = filtered))
                        }
                    },
                    label = { Text(stringResource(R.string.work_record_label_total_quantity)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = AppTextFieldShape,
                    colors = appOutlinedTextFieldColors(),
                    placeholder = {
                        Text(
                            text = "0",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                )
                OutlinedTextField(
                    value = workRecordDetails.serialNumber,
                    onValueChange = { onValueChange(workRecordDetails.copy(serialNumber = it)) },
                    label = { Text(stringResource(R.string.work_record_label_serial_number)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = AppTextFieldShape,
                    colors = appOutlinedTextFieldColors()
                )
            }
        }
    }
}

@Composable
fun workRecordFieldErrorText(error: WorkRecordFieldError?): String? {
    return when (error) {
        WorkRecordFieldError.Required -> stringResource(R.string.work_record_error_required)
        WorkRecordFieldError.InvalidNumber -> stringResource(R.string.work_record_error_invalid_number)
        WorkRecordFieldError.MustBePositive -> stringResource(R.string.work_record_error_must_be_positive)
        WorkRecordFieldError.MustBeNonNegative -> stringResource(R.string.work_record_error_must_be_non_negative)
        null -> null
    }
}
