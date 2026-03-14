package com.example.processrecord.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.ui.component.AppDangerButton
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppDropdownMenu
import com.example.processrecord.ui.component.AppDropdownMenuItem
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.viewmodel.WorkRecordFieldError
import com.example.processrecord.ui.viewmodel.WorkRecordDetails

const val PROCESS_SELECTOR_OVERLAY_TEST_TAG = "process_selector_overlay"
const val ADD_PROCESS_MENU_ITEM_TEST_TAG = "add_process_menu_item"
const val PROCESS_DIALOG_NAME_INPUT_TEST_TAG = "process_dialog_name_input"
const val PROCESS_DIALOG_PRICE_INPUT_TEST_TAG = "process_dialog_price_input"
const val PROCESS_DIALOG_UNIT_INPUT_TEST_TAG = "process_dialog_unit_input"
const val PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG = "process_dialog_confirm_button"
const val STYLE_INPUT_TEST_TAG = "style_input"
const val PROCESS_INPUT_TEST_TAG = "process_input"

@Composable
fun ProcessStyleSectionCard(
    workRecordDetails: WorkRecordDetails,
    processList: List<Process>,
    styleList: List<Style>,
    onValueChange: (WorkRecordDetails) -> Unit,
    onProcessSelected: (Process) -> Unit,
    onStyleSelected: (String) -> Unit,
    onAddProcess: (String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    styleError: WorkRecordFieldError? = null,
    processError: WorkRecordFieldError? = null,
    showAdditionalFields: Boolean = true,
    embedded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val defaultUnit = stringResource(R.string.process_default_unit_value)

    var expandedProcess by remember { mutableStateOf(false) }
    var expandedStyle by remember { mutableStateOf(false) }

    var showProcessDialog by remember { mutableStateOf(false) }
    var editingProcess by remember { mutableStateOf<Process?>(null) }
    var processDialogName by remember { mutableStateOf("") }
    var processDialogPrice by remember { mutableStateOf("") }
    var processDialogUnit by remember { mutableStateOf("") }
    var showDeleteProcessDialog by remember { mutableStateOf<Process?>(null) }

    @Composable
    fun FormContent() {
        val styleFocusRequester = remember { FocusRequester() }

        OutlinedTextField(
            value = workRecordDetails.style,
            onValueChange = { onValueChange(workRecordDetails.copy(style = it)) },
            label = { Text(stringResource(R.string.work_record_label_style)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Style, contentDescription = null) },
            trailingIcon = {
                Box {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.work_record_content_description_style_history),
                        modifier = Modifier.clickable { expandedStyle = true }
                    )
                    AppDropdownMenu(
                        expanded = expandedStyle,
                        onDismissRequest = { expandedStyle = false }
                    ) {
                        if (styleList.isEmpty()) {
                            AppDropdownMenuItem(
                                text = stringResource(R.string.work_record_style_history_empty),
                                onClick = { expandedStyle = false },
                                enabled = false
                            )
                        } else {
                            styleList.forEach { style ->
                                AppDropdownMenuItem(
                                    text = style.name,
                                    onClick = {
                                        onStyleSelected(style.name)
                                        expandedStyle = false
                                    },
                                    selected = workRecordDetails.style == style.name
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(styleFocusRequester)
                .testTag(STYLE_INPUT_TEST_TAG),
            singleLine = true,
            shape = AppTextFieldShape,
            colors = appOutlinedTextFieldColors(),
            isError = styleError != null,
            supportingText = {
                workRecordFieldErrorText(styleError)?.let { Text(text = it) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        LaunchedEffect(Unit) {
            if (workRecordDetails.style.isEmpty()) {
                try {
                    styleFocusRequester.requestFocus()
                } catch (_: Exception) {
                    // Ignore focus failures
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = workRecordDetails.processName,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.work_record_label_process)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PROCESS_INPUT_TEST_TAG),
                shape = AppTextFieldShape,
                colors = appOutlinedTextFieldColors(),
                isError = processError != null,
                supportingText = {
                    workRecordFieldErrorText(processError)?.let { Text(text = it) }
                }
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .testTag(PROCESS_SELECTOR_OVERLAY_TEST_TAG)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expandedProcess = true
                    }
            )

            AppDropdownMenu(
                expanded = expandedProcess,
                onDismissRequest = { expandedProcess = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                AppDropdownMenuItem(
                    modifier = Modifier.testTag(ADD_PROCESS_MENU_ITEM_TEST_TAG),
                    text = stringResource(R.string.process_entry_title_add),
                    leadingIcon = Icons.Default.Add,
                    emphasized = true,
                    onClick = {
                        expandedProcess = false
                        editingProcess = null
                        processDialogName = ""
                        processDialogPrice = ""
                        processDialogUnit = ""
                        showProcessDialog = true
                    }
                )

                if (processList.isNotEmpty()) {
                    HorizontalDivider()
                }

                processList.forEach { process ->
                    AppDropdownMenuItem(
                        text = process.name,
                        supportingText = stringResource(
                            R.string.process_price_per_unit,
                            formatProcessPriceValue(process.defaultPrice),
                            process.unit
                        ),
                        selected = workRecordDetails.processName == process.name,
                        trailingContent = {
                            Row {
                                AppIconActionButton(
                                    onClick = {
                                        expandedProcess = false
                                        editingProcess = process
                                        processDialogName = process.name
                                        processDialogPrice = process.defaultPrice.toString()
                                        processDialogUnit = process.unit
                                        showProcessDialog = true
                                    },
                                    icon = Icons.Default.Edit,
                                    contentDescription = stringResource(
                                        R.string.work_record_content_description_edit_process
                                    ),
                                    size = 32.dp
                                )
                                AppIconActionButton(
                                    onClick = {
                                        expandedProcess = false
                                        showDeleteProcessDialog = process
                                    },
                                    icon = Icons.Default.Delete,
                                    contentDescription = stringResource(
                                        R.string.work_record_content_description_delete_process
                                    ),
                                    tint = MaterialTheme.colorScheme.error,
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.26f),
                                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                                    size = 32.dp
                                )
                            }
                        },
                        onClick = {
                            onProcessSelected(process)
                            expandedProcess = false
                        }
                    )
                }
            }
        }

        if (showProcessDialog) {
            val isAddMode = editingProcess == null
            val normalizedPriceInput = processDialogPrice.trim()
            val parsedPrice = parseProcessPriceInput(normalizedPriceInput) ?: run {
                if (normalizedPriceInput.isEmpty()) 0.0 else null
            }
            val normalizedName = processDialogName.trim()
            val editingProcessId = editingProcess?.id
            val duplicateProcess = processList.firstOrNull { process ->
                process.name.trim().equals(normalizedName, ignoreCase = true) &&
                    (editingProcessId == null || process.id != editingProcessId)
            }
            val isNameDuplicate = normalizedName.isNotEmpty() && duplicateProcess != null
            AppDialogScaffold(
                onDismissRequest = { showProcessDialog = false },
                title = stringResource(
                    if (isAddMode) {
                        R.string.process_entry_title_add
                    } else {
                        R.string.process_entry_title_edit
                    }
                ),
                content = {
                    BoxWithConstraints {
                        val stackMetaFields = maxWidth < 360.dp
                        val nameFocusRequester = remember { FocusRequester() }
                        val priceFocusRequester = remember { FocusRequester() }

                        LaunchedEffect(showProcessDialog) {
                            if (showProcessDialog) {
                                try {
                                    nameFocusRequester.requestFocus()
                                } catch (_: Exception) {
                                    // Ignore focus failures
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(R.string.process_name_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = processDialogName,
                                    onValueChange = { processDialogName = it },
                                    isError = isNameDuplicate,
                                    supportingText = {
                                        if (isNameDuplicate) {
                                            Text(
                                                text = stringResource(
                                                    R.string.process_exists_message,
                                                    normalizedName
                                                )
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(nameFocusRequester)
                                        .testTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG),
                                    shape = AppTextFieldShape,
                                    colors = appOutlinedTextFieldColors(),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                )
                            }

                            ProcessSuffixSelector(
                                name = processDialogName,
                                onNameChange = { processDialogName = it },
                                compact = true
                            )

                            if (stackMetaFields) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = stringResource(R.string.process_default_price_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedTextField(
                                            value = processDialogPrice,
                                            onValueChange = { input ->
                                                val filtered = input.filter { it.isDigit() || it == '.' }
                                                val dotCount = filtered.count { it == '.' }
                                                if (dotCount <= 1) {
                                                    val parts = filtered.split('.')
                                                    if (parts.size <= 2 && (parts.size == 1 || parts[1].length <= 2)) {
                                                        processDialogPrice = filtered
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(priceFocusRequester)
                                                .testTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG),
                                            shape = AppTextFieldShape,
                                            colors = appOutlinedTextFieldColors(),
                                            placeholder = {
                                                Text(
                                                    "0.00",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = stringResource(R.string.process_unit_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedTextField(
                                            value = processDialogUnit,
                                            onValueChange = { processDialogUnit = it },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag(PROCESS_DIALOG_UNIT_INPUT_TEST_TAG),
                                            shape = AppTextFieldShape,
                                            colors = appOutlinedTextFieldColors(),
                                            placeholder = {
                                                Text(
                                                    stringResource(R.string.process_default_unit_value),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.process_default_price_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedTextField(
                                            value = processDialogPrice,
                                            onValueChange = { input ->
                                                val filtered = input.filter { it.isDigit() || it == '.' }
                                                val dotCount = filtered.count { it == '.' }
                                                if (dotCount <= 1) {
                                                    val parts = filtered.split('.')
                                                    if (parts.size <= 2 && (parts.size == 1 || parts[1].length <= 2)) {
                                                        processDialogPrice = filtered
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Next
                                            ),
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(priceFocusRequester)
                                                .testTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG),
                                            shape = AppTextFieldShape,
                                            colors = appOutlinedTextFieldColors(),
                                            placeholder = {
                                                Text(
                                                    "0.00",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.process_unit_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedTextField(
                                            value = processDialogUnit,
                                            onValueChange = { processDialogUnit = it },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag(PROCESS_DIALOG_UNIT_INPUT_TEST_TAG),
                                            shape = AppTextFieldShape,
                                            colors = appOutlinedTextFieldColors(),
                                            placeholder = {
                                                Text(
                                                    stringResource(R.string.process_default_unit_value),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    AppSecondaryButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = { showProcessDialog = false },
                        height = 44.dp
                    )
                    AppPrimaryButton(
                        text = stringResource(
                            if (isAddMode) R.string.common_save else R.string.common_update
                        ),
                        onClick = {
                            val name = normalizedName
                            val price = parsedPrice ?: return@AppPrimaryButton
                            val unit = processDialogUnit.trim().ifEmpty { defaultUnit }
                            if (name.isNotEmpty()) {
                                val editing = editingProcess
                                if (editing == null) {
                                    onAddProcess(name, price, unit)
                                } else {
                                    onUpdateProcess(
                                        editing.copy(
                                            name = name,
                                            defaultPrice = price,
                                            unit = unit
                                        )
                                    )
                                }
                                showProcessDialog = false
                            }
                        },
                        enabled = normalizedName.isNotEmpty() && parsedPrice != null && !isNameDuplicate,
                        modifier = Modifier.testTag(PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG),
                        height = 44.dp
                    )
                }
            )
        }

        showDeleteProcessDialog?.let { process ->
            AppDialogScaffold(
                onDismissRequest = { showDeleteProcessDialog = null },
                title = stringResource(R.string.process_delete_confirm_title),
                supportingText = stringResource(
                    R.string.process_delete_confirm_message,
                    process.name
                ),
                actions = {
                    AppSecondaryButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = { showDeleteProcessDialog = null },
                        height = 44.dp
                    )
                    AppDangerButton(
                        text = stringResource(R.string.common_delete),
                        onClick = {
                            onDeleteProcess(process)
                            showDeleteProcessDialog = null
                        },
                        height = 44.dp
                    )
                }
            )
        }

        if (showAdditionalFields) {
            Spacer(modifier = Modifier.height(10.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stackFields = maxWidth < 420.dp

                if (stackFields) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = workRecordDetails.serialNumber,
                            onValueChange = { onValueChange(workRecordDetails.copy(serialNumber = it)) },
                            label = { Text(stringResource(R.string.work_record_label_serial_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = AppTextFieldShape,
                            colors = appOutlinedTextFieldColors()
                        )
                        OutlinedTextField(
                            value = workRecordDetails.totalQuantity,
                            onValueChange = {
                                onValueChange(workRecordDetails.copy(totalQuantity = it.filter(Char::isDigit)))
                            },
                            label = { Text(stringResource(R.string.work_record_label_total_quantity)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = AppTextFieldShape,
                            colors = appOutlinedTextFieldColors()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = workRecordDetails.serialNumber,
                            onValueChange = { onValueChange(workRecordDetails.copy(serialNumber = it)) },
                            label = { Text(stringResource(R.string.work_record_label_serial_number)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = AppTextFieldShape,
                            colors = appOutlinedTextFieldColors()
                        )
                        OutlinedTextField(
                            value = workRecordDetails.totalQuantity,
                            onValueChange = {
                                onValueChange(workRecordDetails.copy(totalQuantity = it.filter(Char::isDigit)))
                            },
                            label = { Text(stringResource(R.string.work_record_label_total_quantity)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = AppTextFieldShape,
                            colors = appOutlinedTextFieldColors()
                        )
                    }
                }
            }
        }
    }

    if (embedded) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            FormContent()
        }
    } else {
        SectionCard(modifier = modifier) {
            SectionHeader(title = stringResource(R.string.work_record_section_process_style)) {
                Icon(
                    imageVector = Icons.Default.Sell,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            FormContent()
        }
    }
}
