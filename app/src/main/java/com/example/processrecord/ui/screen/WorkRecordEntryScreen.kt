package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppDangerButton
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.viewmodel.InvalidProcessInputException
import com.example.processrecord.ui.viewmodel.InvalidWorkRecordInputException
import com.example.processrecord.ui.viewmodel.ProcessAlreadyExistsException
import com.example.processrecord.ui.viewmodel.ProcessOperationFailedException
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordNotFoundException
import com.example.processrecord.ui.viewmodel.WorkRecordUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val WORK_RECORD_SAVE_BUTTON_TEST_TAG = "work_record_save_button"

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
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = title,
                subtitle = stringResource(
                    if (viewModel.workRecordUiState.workRecordDetails.id == 0L) {
                        R.string.work_record_subtitle_add
                    } else {
                        R.string.work_record_subtitle_edit
                    }
                ),
                navigationIcon = {
                    ChromeIconButton(
                        onClick = navigateBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back)
                    )
                }
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
                    withContext(Dispatchers.Main.immediate) {
                        result.fold(
                            onSuccess = { navigateBack() },
                            onFailure = { error ->
                                if (error !is InvalidWorkRecordInputException) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.work_record_save_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            },
            onDeleteClick = {
                coroutineScope.launch {
                    val result = viewModel.deleteRecord()
                    withContext(Dispatchers.Main.immediate) {
                        result.fold(
                            onSuccess = { navigateBack() },
                            onFailure = { error ->
                                val message = when (error) {
                                    is WorkRecordNotFoundException -> context.getString(
                                        R.string.work_record_not_found
                                    )
                                    else -> context.getString(R.string.work_record_delete_failed)
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WorkRecordInputForm(
            workRecordUiState = workRecordUiState,
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

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppPrimaryButton(
                text = saveButtonText,
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG),
                icon = Icons.Default.Check
            )
            if (workRecordUiState.workRecordDetails.id != 0L) {
                AppDangerButton(
                    text = stringResource(R.string.work_record_button_delete),
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Delete
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDeleteConfirm) {
        AppDialogScaffold(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(R.string.work_record_delete_confirm_title),
            supportingText = stringResource(R.string.work_record_delete_confirm_message),
            actions = {
                AppSecondaryButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = { showDeleteConfirm = false },
                    height = 44.dp
                )
                AppDangerButton(
                    text = stringResource(R.string.common_delete),
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    },
                    height = 44.dp
                )
            }
        )
    }
}

@Composable
fun WorkRecordInputForm(
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
    modifier: Modifier = Modifier
) {
    val details = workRecordUiState.workRecordDetails

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BasicInfoSectionCard(
            workRecordDetails = details,
            onValueChange = onValueChange
        )

        ProcessStyleSectionCard(
            workRecordDetails = details,
            processList = processList,
            styleList = styleList,
            onValueChange = onValueChange,
            onProcessSelected = onProcessSelected,
            onStyleSelected = onStyleSelected,
            onAddProcess = onAddProcess,
            onUpdateProcess = onUpdateProcess,
            onDeleteProcess = onDeleteProcess,
            styleError = workRecordUiState.validationErrors.style,
            processError = workRecordUiState.validationErrors.processName
        )

        ColorDetailSectionCard(
            workRecordDetails = details,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onAddColorEntryFromPreset = onAddColorEntryFromPreset,
            onUpdateColorEntryQuantity = onUpdateColorEntryQuantity,
            onUpdateColorEntryDeficit = onUpdateColorEntryDeficit,
            onUpdateColorEntryColorCode = onUpdateColorEntryColorCode,
            onRemoveColorEntry = onRemoveColorEntry,
            onManageColorPresetsClick = onManageColorPresetsClick
        )

        RecordAmountSectionCard(
            workRecordDetails = details,
            onValueChange = onValueChange,
            quantityError = workRecordUiState.validationErrors.quantity,
            unitPriceError = workRecordUiState.validationErrors.unitPrice
        )

        RecordTimeSectionCard(
            workRecordDetails = details,
            onValueChange = onValueChange
        )

        RecordRemarkSectionCard(
            workRecordDetails = details,
            onValueChange = onValueChange
        )
    }
}
