package com.example.processrecord.ui.screen
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.InvalidProcessInputException
import com.example.processrecord.ui.viewmodel.InvalidWorkRecordInputException
import com.example.processrecord.ui.viewmodel.ProcessAlreadyExistsException
import com.example.processrecord.ui.viewmodel.ProcessOperationFailedException
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordNotFoundException
import com.example.processrecord.ui.viewmodel.WorkRecordUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordEntryScreen(
    viewModel: WorkRecordEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit,
    navigateToColorPresetManage: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val processList by viewModel.processList.collectAsState()
    val styleList by viewModel.styleList.collectAsState()
    val colorGroups by viewModel.colorGroups.collectAsState()
    val colorPresets by viewModel.colorPresets.collectAsState()
    val title = if (viewModel.workRecordUiState.workRecordDetails.id == 0L) {
        stringResource(R.string.work_record_title_add)
    } else {
        stringResource(R.string.work_record_title_edit)
    }
    val processOperationError = viewModel.processOperationError

    LaunchedEffect(processOperationError) {
        val error = processOperationError ?: return@LaunchedEffect
        val message = when (error) {
            is ProcessAlreadyExistsException -> context.getString(
                R.string.process_exists_message,
                error.processName
            )
            is InvalidProcessInputException -> context.getString(R.string.process_input_invalid)
            is ProcessOperationFailedException -> context.getString(R.string.process_operation_failed)
            else -> context.getString(R.string.process_save_failed)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.consumeProcessOperationError()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        WorkRecordEntryBody(
            workRecordUiState = viewModel.workRecordUiState,
            processList = processList,
            styleList = styleList,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onValueChange = viewModel::updateUiState,
            onProcessSelected = viewModel::onProcessSelected,
            onStyleSelected = viewModel::onStyleSelected,
            onAddProcess = viewModel::addProcess,
            onUpdateProcess = viewModel::updateProcess,
            onDeleteProcess = viewModel::deleteProcess,
            onAddColorEntryFromPreset = viewModel::addColorEntryFromPreset,
            onUpdateColorEntryQuantity = viewModel::updateColorEntryQuantity,
            onUpdateColorEntryDeficit = viewModel::updateColorEntryDeficit,
            onUpdateColorEntryColorCode = viewModel::updateColorEntryColorCode,
            onRemoveColorEntry = viewModel::removeColorEntry,
            onManageColorPresetsClick = navigateToColorPresetManage,
            onSaveClick = {
                coroutineScope.launch {
                    val result = viewModel.saveWorkRecord()
                    result.fold(
                        onSuccess = { navigateBack() },
                        onFailure = { error ->
                            val message = when (error) {
                                is InvalidWorkRecordInputException -> context.getString(
                                    R.string.work_record_input_invalid
                                )
                                else -> context.getString(R.string.work_record_save_failed)
                            }
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            },
            onDeleteClick = {
                coroutineScope.launch {
                    val result = viewModel.deleteRecord()
                    result.fold(
                        onSuccess = { navigateBack() },
                        onFailure = { error ->
                            val message = when (error) {
                                is WorkRecordNotFoundException -> context.getString(
                                    R.string.work_record_not_found
                                )
                                else -> context.getString(R.string.work_record_delete_failed)
                            }
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun WorkRecordEntryBody(
    workRecordUiState: WorkRecordUiState,
    processList: List<Process>,
    styleList: List<Style>,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    onValueChange: (WorkRecordDetails) -> Unit,
    onProcessSelected: (Process) -> Unit,
    onStyleSelected: (String) -> Unit,
    onAddProcess: (String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    onAddColorEntryFromPreset: (String, String) -> Unit,
    onUpdateColorEntryQuantity: (String, String) -> Unit,
    onUpdateColorEntryDeficit: (String, String) -> Unit,
    onUpdateColorEntryColorCode: (String, String) -> Unit,
    onRemoveColorEntry: (String) -> Unit,
    onManageColorPresetsClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val saveButtonText = if (workRecordUiState.workRecordDetails.id == 0L) {
        stringResource(R.string.work_record_button_save)
    } else {
        stringResource(R.string.work_record_button_update)
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WorkRecordInputForm(
            workRecordDetails = workRecordUiState.workRecordDetails,
            processList = processList,
            styleList = styleList,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onValueChange = onValueChange,
            onProcessSelected = onProcessSelected,
            onStyleSelected = onStyleSelected,
            onAddProcess = onAddProcess,
            onUpdateProcess = onUpdateProcess,
            onDeleteProcess = onDeleteProcess,
            onAddColorEntryFromPreset = onAddColorEntryFromPreset,
            onUpdateColorEntryQuantity = onUpdateColorEntryQuantity,
            onUpdateColorEntryDeficit = onUpdateColorEntryDeficit,
            onUpdateColorEntryColorCode = onUpdateColorEntryColorCode,
            onRemoveColorEntry = onRemoveColorEntry,
            onManageColorPresetsClick = onManageColorPresetsClick
        )

        // Save/Delete action area.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onSaveClick,
                enabled = workRecordUiState.isEntryValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = saveButtonText,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            if (workRecordUiState.workRecordDetails.id != 0L) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.work_record_button_delete),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecordInputForm(
    workRecordDetails: WorkRecordDetails,
    processList: List<Process>,
    styleList: List<Style>,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    onValueChange: (WorkRecordDetails) -> Unit,
    onProcessSelected: (Process) -> Unit,
    onStyleSelected: (String) -> Unit,
    onAddProcess: (String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    onAddColorEntryFromPreset: (String, String) -> Unit,
    onUpdateColorEntryQuantity: (String, String) -> Unit,
    onUpdateColorEntryDeficit: (String, String) -> Unit,
    onUpdateColorEntryColorCode: (String, String) -> Unit,
    onRemoveColorEntry: (String) -> Unit,
    onManageColorPresetsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {

        BasicInfoSectionCard(
            workRecordDetails = workRecordDetails,
            onValueChange = onValueChange
        )

        // Style and process section.
        ProcessStyleSectionCard(
            workRecordDetails = workRecordDetails,
            processList = processList,
            styleList = styleList,
            onValueChange = onValueChange,
            onProcessSelected = onProcessSelected,
            onStyleSelected = onStyleSelected,
            onAddProcess = onAddProcess,
            onUpdateProcess = onUpdateProcess,
            onDeleteProcess = onDeleteProcess
        )

        // Color details section.
        ColorDetailSectionCard(
            workRecordDetails = workRecordDetails,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onAddColorEntryFromPreset = onAddColorEntryFromPreset,
            onUpdateColorEntryQuantity = onUpdateColorEntryQuantity,
            onUpdateColorEntryDeficit = onUpdateColorEntryDeficit,
            onUpdateColorEntryColorCode = onUpdateColorEntryColorCode,
            onRemoveColorEntry = onRemoveColorEntry,
            onManageColorPresetsClick = onManageColorPresetsClick
        )

        // Quantity and amount section.
        SectionCard {
            SectionHeader(title = stringResource(R.string.work_record_section_quantity_amount)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Quantity and unit price inputs.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = workRecordDetails.quantity,
                    onValueChange = { input ->
                        onValueChange(workRecordDetails.copy(quantity = input.filter(Char::isDigit)))
                    },
                    label = { Text(stringResource(R.string.work_record_label_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = workRecordDetails.unitPrice,
                    onValueChange = { onValueChange(workRecordDetails.copy(unitPrice = it)) },
                    label = { Text(stringResource(R.string.work_record_label_unit_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total amount preview.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        RecordTimeSectionCard(
            workRecordDetails = workRecordDetails,
            onValueChange = onValueChange
        )

        // Optional remark section.
        SectionCard {
            SectionHeader(title = stringResource(R.string.work_record_section_remark_optional)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedTextField(
                value = workRecordDetails.remark,
                onValueChange = { onValueChange(workRecordDetails.copy(remark = it)) },
                label = { Text(stringResource(R.string.work_record_label_remark)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}


