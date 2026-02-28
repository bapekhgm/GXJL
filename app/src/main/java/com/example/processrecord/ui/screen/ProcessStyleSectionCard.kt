package com.example.processrecord.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.ui.viewmodel.WorkRecordDetails

const val PROCESS_SELECTOR_OVERLAY_TEST_TAG = "process_selector_overlay"
const val ADD_PROCESS_MENU_ITEM_TEST_TAG = "add_process_menu_item"
const val PROCESS_DIALOG_NAME_INPUT_TEST_TAG = "process_dialog_name_input"
const val PROCESS_DIALOG_PRICE_INPUT_TEST_TAG = "process_dialog_price_input"
const val PROCESS_DIALOG_UNIT_INPUT_TEST_TAG = "process_dialog_unit_input"
const val PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG = "process_dialog_confirm_button"
private const val DEFAULT_PROCESS_PRICE_INPUT = "0"

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
    onDeleteProcess: (Process) -> Unit
) {
    val defaultUnit = stringResource(R.string.process_default_unit_value)

    var expandedProcess by remember { mutableStateOf(false) }
    var expandedStyle by remember { mutableStateOf(false) }

    var showProcessDialog by remember { mutableStateOf(false) }
    var editingProcess by remember { mutableStateOf<Process?>(null) }
    var processDialogName by remember { mutableStateOf("") }
    var processDialogPrice by remember { mutableStateOf(DEFAULT_PROCESS_PRICE_INPUT) }
    var processDialogUnit by remember(defaultUnit) { mutableStateOf(defaultUnit) }
    var showDeleteProcessDialog by remember { mutableStateOf<Process?>(null) }

    SectionCard {
        SectionHeader(title = stringResource(R.string.work_record_section_process_style)) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedTextField(
            value = workRecordDetails.style,
            onValueChange = { onValueChange(workRecordDetails.copy(style = it)) },
            label = { Text(stringResource(R.string.work_record_label_style)) },
            leadingIcon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null) },
            trailingIcon = {
                Box {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.work_record_content_description_style_history),
                        modifier = Modifier.clickable { expandedStyle = true }
                    )
                    DropdownMenu(
                        expanded = expandedStyle,
                        onDismissRequest = { expandedStyle = false }
                    ) {
                        if (styleList.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.work_record_style_history_empty),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { expandedStyle = false }
                            )
                        } else {
                            styleList.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.name) },
                                    onClick = {
                                        onStyleSelected(style.name)
                                        expandedStyle = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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

            DropdownMenu(
                expanded = expandedProcess,
                onDismissRequest = { expandedProcess = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                DropdownMenuItem(
                    modifier = Modifier.testTag(ADD_PROCESS_MENU_ITEM_TEST_TAG),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.process_entry_title_add),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    onClick = {
                        expandedProcess = false
                        editingProcess = null
                        processDialogName = ""
                        processDialogPrice = DEFAULT_PROCESS_PRICE_INPUT
                        processDialogUnit = defaultUnit
                        showProcessDialog = true
                    }
                )

                if (processList.isNotEmpty()) {
                    HorizontalDivider()
                }

                processList.forEach { process ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = process.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(
                                        R.string.process_price_per_unit,
                                        formatProcessPriceValue(process.defaultPrice),
                                        process.unit
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        trailingIcon = {
                            Row {
                                IconButton(
                                    onClick = {
                                        expandedProcess = false
                                        editingProcess = process
                                        processDialogName = process.name
                                        processDialogPrice = process.defaultPrice.toString()
                                        processDialogUnit = process.unit
                                        showProcessDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(
                                            R.string.work_record_content_description_edit_process
                                        ),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        expandedProcess = false
                                        showDeleteProcessDialog = process
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(
                                            R.string.work_record_content_description_delete_process
                                        ),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
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
            val parsedPrice = parseProcessPriceInput(processDialogPrice)
            val normalizedName = processDialogName.trim()
            val editingProcessId = editingProcess?.id
            val duplicateProcess = processList.firstOrNull { process ->
                process.name.trim().equals(normalizedName, ignoreCase = true) &&
                    (editingProcessId == null || process.id != editingProcessId)
            }
            val isNameDuplicate = normalizedName.isNotEmpty() && duplicateProcess != null
            AlertDialog(
                onDismissRequest = { showProcessDialog = false },
                title = {
                    Text(
                        text = stringResource(
                            if (isAddMode) {
                                R.string.process_entry_title_add
                            } else {
                                R.string.process_entry_title_edit
                            }
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = processDialogName,
                            onValueChange = { processDialogName = it },
                            label = { Text(stringResource(R.string.process_name_label)) },
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
                                .testTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ProcessSuffixSelector(
                            name = processDialogName,
                            onNameChange = { processDialogName = it }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = processDialogPrice,
                                onValueChange = { processDialogPrice = it },
                                label = { Text(stringResource(R.string.process_default_price_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = processDialogUnit,
                                onValueChange = { processDialogUnit = it },
                                label = { Text(stringResource(R.string.process_unit_label)) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(PROCESS_DIALOG_UNIT_INPUT_TEST_TAG),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.testTag(PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG),
                        onClick = {
                            val name = normalizedName
                            val price = parsedPrice ?: return@TextButton
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
                        enabled = normalizedName.isNotEmpty() && parsedPrice != null && !isNameDuplicate
                    ) {
                        Text(
                            text = stringResource(
                                if (isAddMode) R.string.common_save else R.string.common_update
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProcessDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        showDeleteProcessDialog?.let { process ->
            AlertDialog(
                onDismissRequest = { showDeleteProcessDialog = null },
                title = { Text(stringResource(R.string.process_delete_confirm_title)) },
                text = {
                    Text(
                        text = stringResource(
                            R.string.process_delete_confirm_message,
                            process.name
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteProcess(process)
                        showDeleteProcessDialog = null
                    }) {
                        Text(
                            text = stringResource(R.string.common_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteProcessDialog = null }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = workRecordDetails.totalQuantity,
                onValueChange = { onValueChange(workRecordDetails.copy(totalQuantity = it)) },
                label = { Text(stringResource(R.string.work_record_label_total_quantity)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
