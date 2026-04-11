package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppActionChip
import com.example.processrecord.ui.component.AppDangerButton
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppDropdownMenu
import com.example.processrecord.ui.component.AppDropdownMenuItem
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors
import com.example.processrecord.ui.viewmodel.ExistingWorkRecordGroupUiState
import com.example.processrecord.ui.viewmodel.InvalidProcessInputException
import com.example.processrecord.ui.viewmodel.InvalidWorkRecordInputException
import com.example.processrecord.ui.viewmodel.ProcessAlreadyExistsException
import com.example.processrecord.ui.viewmodel.ProcessOperationFailedException
import com.example.processrecord.ui.viewmodel.WorkRecordDetails
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordFieldError
import com.example.processrecord.ui.viewmodel.WorkRecordGroupUiState
import com.example.processrecord.ui.viewmodel.WorkRecordNotFoundException
import com.example.processrecord.ui.viewmodel.WorkRecordValidationErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val WORK_RECORD_SAVE_BUTTON_TEST_TAG = "work_record_save_button"
const val WORK_RECORD_ADD_PROCESS_BUTTON_TEST_TAG = "work_record_add_process_button"

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
    val existingGroupUiState = viewModel.existingGroupUiState
    val groupUiState = viewModel.groupUiState
    val groupSharedDetails = viewModel.groupSharedDetails
    val initialExistingItemIndex = viewModel.initialExistingItemIndex
    val title = stringResource(
        if (viewModel.isSingleRecordEditMode) {
            R.string.work_record_title_edit
        } else {
            R.string.work_record_title_group_add
        }
    )
    val subtitle = stringResource(
        if (viewModel.isSingleRecordEditMode) {
            R.string.work_record_subtitle_edit
        } else if (viewModel.isAppendToExistingGroupMode) {
            R.string.work_record_append_subtitle
        } else {
            R.string.work_record_group_subtitle_add
        }
    )
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
                subtitle = subtitle,
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
            groupUiState = groupUiState,
            isSingleRecordEditMode = viewModel.isSingleRecordEditMode,
            isAppendToExistingGroupMode = viewModel.isAppendToExistingGroupMode,
            existingGroupUiState = existingGroupUiState,
            groupSharedDetails = groupSharedDetails,
            initialExistingItemIndex = initialExistingItemIndex,
            processList = processList,
            styleList = styleList,
            colorGroups = colorGroups,
            colorPresets = colorPresets,
            onStyleChange = viewModel::updateGroupStyle,
            onStyleSelected = viewModel::onStyleSelected,
            onSharedDateChange = viewModel::updateSharedDate,
            onSharedTotalQuantityChange = viewModel::updateSharedTotalQuantity,
            onSharedImagePathsChange = viewModel::updateSharedImagePaths,
            onAddProcessItem = viewModel::addProcessItem,
            onRemoveProcessItem = viewModel::removeProcessItem,
            onItemValueChange = viewModel::updateProcessItem,
            onProcessSelected = viewModel::onProcessSelected,
            onAddProcess = viewModel::addProcess,
            onAddProcessToExisting = viewModel::addProcessToExisting,
            onUpdateProcess = viewModel::updateProcess,
            onDeleteProcess = viewModel::deleteProcess,
            onAddColorEntryFromPreset = viewModel::addColorEntryFromPreset,
            onAddColorEntryFromExistingPreset = viewModel::addColorEntryFromExistingPreset,
            onUpdateColorEntryQuantity = viewModel::updateColorEntryQuantity,
            onUpdateExistingColorEntryQuantity = viewModel::updateExistingColorEntryQuantity,
            onUpdateColorEntryDeficit = viewModel::updateColorEntryDeficit,
            onUpdateExistingColorEntryDeficit = viewModel::updateExistingColorEntryDeficit,
            onToggleColorEntryDeficitResolved = viewModel::toggleColorEntryDeficitResolved,
            onToggleExistingColorEntryDeficitResolved = viewModel::toggleExistingColorEntryDeficitResolved,
            onUpdateColorEntryColorCode = viewModel::updateColorEntryColorCode,
            onUpdateExistingColorEntryColorCode = viewModel::updateExistingColorEntryColorCode,
            onRemoveColorEntry = viewModel::removeColorEntry,
            onRemoveExistingColorEntry = viewModel::removeExistingColorEntry,
            onManageColorPresetsClick = navigateToColorPresetManage,
            onExistingItemValueChange = viewModel::updateExistingProcessItem,
            onExistingProcessSelected = viewModel::onExistingProcessSelected,
            onSaveExistingRecord = { index ->
                coroutineScope.launch {
                    val result = viewModel.saveExistingRecord(index)
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
            onDeleteExistingRecord = viewModel::deleteExistingGroupRecord,
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
    groupUiState: WorkRecordGroupUiState,
    isSingleRecordEditMode: Boolean,
    isAppendToExistingGroupMode: Boolean,
    existingGroupUiState: ExistingWorkRecordGroupUiState,
    groupSharedDetails: WorkRecordDetails,
    initialExistingItemIndex: Int?,
    processList: List<Process>,
    styleList: List<Style>,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    onStyleChange: (String) -> Unit,
    onStyleSelected: (String) -> Unit,
    onSharedDateChange: (Long) -> Unit,
    onSharedTotalQuantityChange: (String) -> Unit,
    onSharedImagePathsChange: (List<String>) -> Unit,
    onAddProcessItem: () -> Unit,
    onRemoveProcessItem: (Int) -> Unit,
    onItemValueChange: (Int, WorkRecordDetails) -> Unit,
    onProcessSelected: (Int, Process) -> Unit,
    onAddProcess: (Int, String, Double, String) -> Unit,
    onAddProcessToExisting: (Int, String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    onAddColorEntryFromPreset: (Int, String, String) -> Unit,
    onAddColorEntryFromExistingPreset: (Int, String, String) -> Unit,
    onUpdateColorEntryQuantity: (Int, String, String) -> Unit,
    onUpdateExistingColorEntryQuantity: (Int, String, String) -> Unit,
    onUpdateColorEntryDeficit: (Int, String, String) -> Unit,
    onUpdateExistingColorEntryDeficit: (Int, String, String) -> Unit,
    onToggleColorEntryDeficitResolved: (Int, String) -> Unit,
    onToggleExistingColorEntryDeficitResolved: (Int, String) -> Unit,
    onUpdateColorEntryColorCode: (Int, String, String) -> Unit,
    onUpdateExistingColorEntryColorCode: (Int, String, String) -> Unit,
    onRemoveColorEntry: (Int, String) -> Unit,
    onRemoveExistingColorEntry: (Int, String) -> Unit,
    onManageColorPresetsClick: () -> Unit,
    onExistingItemValueChange: (Int, WorkRecordDetails) -> Unit,
    onExistingProcessSelected: (Int, Process) -> Unit,
    onSaveExistingRecord: (Int) -> Unit,
    onDeleteExistingRecord: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingScrollToNewDraftItem by remember { mutableStateOf(false) }
    var selectedAppendPageIndex by remember(existingGroupUiState.entryGroupId) {
        mutableIntStateOf(0)
    }
    var hasAppliedInitialExistingItemIndex by remember(existingGroupUiState.entryGroupId) {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    val isAppendPagerMode = isAppendToExistingGroupMode && !isSingleRecordEditMode
    val isGroupSharedMode = !isSingleRecordEditMode
    val existingItemCount = existingGroupUiState.items.size
    val draftItemCount = groupUiState.items.size
    val totalAppendPageCount = (existingItemCount + draftItemCount).coerceAtLeast(1)

    LaunchedEffect(
        isAppendPagerMode,
        existingGroupUiState.entryGroupId,
        existingGroupUiState.isLoading,
        existingItemCount,
        initialExistingItemIndex,
        hasAppliedInitialExistingItemIndex
    ) {
        if (!isAppendPagerMode || hasAppliedInitialExistingItemIndex || existingGroupUiState.isLoading) {
            return@LaunchedEffect
        }
        if (initialExistingItemIndex != null && initialExistingItemIndex in 0 until existingItemCount) {
            selectedAppendPageIndex = initialExistingItemIndex
        }
        hasAppliedInitialExistingItemIndex = true
    }

    LaunchedEffect(existingItemCount, draftItemCount) {
        val lastIndex = totalAppendPageCount - 1
        selectedAppendPageIndex = when {
            lastIndex < 0 -> 0
            selectedAppendPageIndex > lastIndex -> lastIndex
            else -> selectedAppendPageIndex
        }
    }

    LaunchedEffect(draftItemCount, pendingScrollToNewDraftItem, isAppendPagerMode) {
        if (!pendingScrollToNewDraftItem || isAppendPagerMode) return@LaunchedEffect
        scrollState.animateScrollTo(scrollState.maxValue)
        pendingScrollToNewDraftItem = false
    }

    val isCurrentExistingPage = isAppendPagerMode && selectedAppendPageIndex < existingItemCount
    val currentExistingIndex = selectedAppendPageIndex
    val currentDraftIndex = (selectedAppendPageIndex - existingItemCount).coerceAtLeast(0)
    val currentAppendDetails = when {
        isCurrentExistingPage -> existingGroupUiState.items.getOrNull(currentExistingIndex)
        isAppendPagerMode -> groupUiState.items.getOrNull(currentDraftIndex)
        else -> null
    }
    val currentAppendValidationErrors = when {
        isCurrentExistingPage -> existingGroupUiState.itemValidationErrors.getOrNull(currentExistingIndex)
        isAppendPagerMode -> groupUiState.itemValidationErrors.getOrNull(currentDraftIndex)
        else -> null
    } ?: WorkRecordValidationErrors()
    val currentStyleError = if (isCurrentExistingPage) {
        currentAppendValidationErrors.style
    } else {
        groupUiState.styleError
    }
    val showTopBasicInfo = isGroupSharedMode
    val topBasicInfoDetails = when {
        isAppendPagerMode -> currentAppendDetails ?: groupUiState.items.firstOrNull()
        isGroupSharedMode -> WorkRecordDetails(date = groupSharedDetails.date)
        else -> null
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showTopBasicInfo && topBasicInfoDetails != null) {
            BasicInfoSectionCard(
                workRecordDetails = topBasicInfoDetails,
                onValueChange = { updatedDetails ->
                    when {
                        isAppendPagerMode && isCurrentExistingPage -> {
                            onExistingItemValueChange(currentExistingIndex, updatedDetails)
                        }

                        isAppendPagerMode -> {
                            onItemValueChange(currentDraftIndex, updatedDetails)
                        }

                        else -> {
                            onSharedDateChange(updatedDetails.date)
                        }
                    }
                },
                showImages = false
            )
        }

        StyleHeaderCard(
            style = groupUiState.style,
            styleList = styleList,
            styleError = currentStyleError,
            onStyleChange = onStyleChange,
            onStyleSelected = onStyleSelected
        )

        if (isGroupSharedMode) {
            GroupSharedImageCard(
                imagePaths = groupSharedDetails.imagePaths,
                onImagePathsChange = onSharedImagePathsChange
            )
        }

        if (isAppendPagerMode) {
            val currentDetails = currentAppendDetails ?: groupUiState.items.first()
            ProcessItemFormCard(
                pageNumber = selectedAppendPageIndex + 1,
                totalPageCount = totalAppendPageCount,
                workRecordDetails = currentDetails,
                processList = processList,
                styleList = styleList,
                colorGroups = colorGroups,
                colorPresets = colorPresets,
                validationErrors = currentAppendValidationErrors,
                showNavigationControls = totalAppendPageCount > 1,
                showBasicInfo = !showTopBasicInfo,
                showImagesInBasicInfo = !isGroupSharedMode,
                showTotalQuantityField = true,
                sharedTotalQuantity = groupSharedDetails.totalQuantity.takeIf { isGroupSharedMode },
                onSharedTotalQuantityChange = onSharedTotalQuantityChange.takeIf { isGroupSharedMode },
                canNavigateToPrevious = selectedAppendPageIndex > 0,
                canNavigateToNext = selectedAppendPageIndex < totalAppendPageCount - 1,
                onShowPrevious = {
                    if (selectedAppendPageIndex > 0) {
                        selectedAppendPageIndex -= 1
                    }
                },
                onShowNext = {
                    if (selectedAppendPageIndex < totalAppendPageCount - 1) {
                        selectedAppendPageIndex += 1
                    }
                },
                canRemove = !isCurrentExistingPage && groupUiState.items.size > 1,
                onRemove = {
                    if (groupUiState.items.size > 1) {
                        onRemoveProcessItem(currentDraftIndex)
                    }
                },
                onValueChange = {
                    if (isCurrentExistingPage) {
                        onExistingItemValueChange(currentExistingIndex, it)
                    } else {
                        onItemValueChange(currentDraftIndex, it)
                    }
                },
                onProcessSelected = {
                    if (isCurrentExistingPage) {
                        onExistingProcessSelected(currentExistingIndex, it)
                    } else {
                        onProcessSelected(currentDraftIndex, it)
                    }
                },
                onAddProcess = { name, price, unit ->
                    if (isCurrentExistingPage) {
                        onAddProcessToExisting(currentExistingIndex, name, price, unit)
                    } else {
                        onAddProcess(currentDraftIndex, name, price, unit)
                    }
                },
                onUpdateProcess = onUpdateProcess,
                onDeleteProcess = onDeleteProcess,
                onAddColorEntryFromPreset = { name, hex ->
                    if (isCurrentExistingPage) {
                        onAddColorEntryFromExistingPreset(currentExistingIndex, name, hex)
                    } else {
                        onAddColorEntryFromPreset(currentDraftIndex, name, hex)
                    }
                },
                onUpdateColorEntryQuantity = { name, quantity ->
                    if (isCurrentExistingPage) {
                        onUpdateExistingColorEntryQuantity(currentExistingIndex, name, quantity)
                    } else {
                        onUpdateColorEntryQuantity(currentDraftIndex, name, quantity)
                    }
                },
                onUpdateColorEntryDeficit = { name, deficit ->
                    if (isCurrentExistingPage) {
                        onUpdateExistingColorEntryDeficit(currentExistingIndex, name, deficit)
                    } else {
                        onUpdateColorEntryDeficit(currentDraftIndex, name, deficit)
                    }
                },
                onToggleColorEntryDeficitResolved = { name ->
                    if (isCurrentExistingPage) {
                        onToggleExistingColorEntryDeficitResolved(currentExistingIndex, name)
                    } else {
                        onToggleColorEntryDeficitResolved(currentDraftIndex, name)
                    }
                },
                onUpdateColorEntryColorCode = { name, code ->
                    if (isCurrentExistingPage) {
                        onUpdateExistingColorEntryColorCode(currentExistingIndex, name, code)
                    } else {
                        onUpdateColorEntryColorCode(currentDraftIndex, name, code)
                    }
                },
                onRemoveColorEntry = { name ->
                    if (isCurrentExistingPage) {
                        onRemoveExistingColorEntry(currentExistingIndex, name)
                    } else {
                        onRemoveColorEntry(currentDraftIndex, name)
                    }
                },
                onManageColorPresetsClick = onManageColorPresetsClick
            )
        } else {
            groupUiState.items.forEachIndexed { index, details ->
                ProcessItemFormCard(
                    pageNumber = index + 1,
                    totalPageCount = groupUiState.items.size,
                    workRecordDetails = details,
                    processList = processList,
                    styleList = styleList,
                    colorGroups = colorGroups,
                    colorPresets = colorPresets,
                    validationErrors = groupUiState.itemValidationErrors.getOrNull(index)
                        ?: WorkRecordValidationErrors(),
                    showNavigationControls = false,
                    showBasicInfo = !showTopBasicInfo,
                    showImagesInBasicInfo = !isGroupSharedMode,
                    showTotalQuantityField = true,
                    sharedTotalQuantity = groupSharedDetails.totalQuantity.takeIf { isGroupSharedMode },
                    onSharedTotalQuantityChange = onSharedTotalQuantityChange.takeIf { isGroupSharedMode },
                    canNavigateToPrevious = false,
                    canNavigateToNext = false,
                    onShowPrevious = {},
                    onShowNext = {},
                    canRemove = groupUiState.items.size > 1,
                    onRemove = { onRemoveProcessItem(index) },
                    onValueChange = { onItemValueChange(index, it) },
                    onProcessSelected = { onProcessSelected(index, it) },
                    onAddProcess = { name, price, unit -> onAddProcess(index, name, price, unit) },
                    onUpdateProcess = onUpdateProcess,
                    onDeleteProcess = onDeleteProcess,
                    onAddColorEntryFromPreset = { name, hex -> onAddColorEntryFromPreset(index, name, hex) },
                    onUpdateColorEntryQuantity = { name, quantity -> onUpdateColorEntryQuantity(index, name, quantity) },
                    onUpdateColorEntryDeficit = { name, deficit -> onUpdateColorEntryDeficit(index, name, deficit) },
                    onToggleColorEntryDeficitResolved = { name ->
                        onToggleColorEntryDeficitResolved(index, name)
                    },
                    onUpdateColorEntryColorCode = { name, code -> onUpdateColorEntryColorCode(index, name, code) },
                    onRemoveColorEntry = { name -> onRemoveColorEntry(index, name) },
                    onManageColorPresetsClick = onManageColorPresetsClick
                )
            }
        }

        if (!isSingleRecordEditMode) {
            AppSecondaryButton(
                text = stringResource(R.string.work_record_group_add_process_button),
                onClick = {
                    onAddProcessItem()
                    if (isAppendPagerMode) {
                        selectedAppendPageIndex = totalAppendPageCount
                    } else {
                        pendingScrollToNewDraftItem = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(WORK_RECORD_ADD_PROCESS_BUTTON_TEST_TAG),
                icon = Icons.Default.Add
            )
        }

        AppPrimaryButton(
            text = stringResource(
                if (isSingleRecordEditMode || isCurrentExistingPage) {
                    R.string.work_record_button_update
                } else {
                    R.string.work_record_group_button_save
                }
            ),
            onClick = {
                if (isCurrentExistingPage) {
                    onSaveExistingRecord(currentExistingIndex)
                } else {
                    onSaveClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG),
            icon = Icons.Default.Check
        )
        if (isSingleRecordEditMode || isCurrentExistingPage) {
            AppDangerButton(
                text = stringResource(R.string.work_record_button_delete),
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Delete
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showDeleteConfirm) {
        AppDialogScaffold(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(R.string.work_record_delete_confirm_title),
            supportingText = stringResource(
                if (isCurrentExistingPage) {
                    R.string.work_record_group_delete_item_message
                } else {
                    R.string.work_record_delete_confirm_message
                }
            ),
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
                        if (isCurrentExistingPage) {
                            onDeleteExistingRecord(currentExistingIndex)
                        } else {
                            onDeleteClick()
                        }
                    },
                    height = 44.dp
                )
            }
        )
    }
}

@Composable
private fun GroupSharedImageCard(
    imagePaths: List<String>,
    onImagePathsChange: (List<String>) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth(), emphasized = true) {
        RecordImageSectionCard(
            workRecordDetails = WorkRecordDetails(
                imagePaths = imagePaths
            ),
            onValueChange = { onImagePathsChange(it.imagePaths) }
        )
    }
}

@Composable
private fun StyleHeaderCard(
    style: String,
    styleList: List<Style>,
    styleError: WorkRecordFieldError?,
    onStyleChange: (String) -> Unit,
    onStyleSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SectionCard(modifier = Modifier.fillMaxWidth(), emphasized = true) {
        SectionHeader(
            title = stringResource(R.string.work_record_group_style_title),
            emphasized = true
        ) {
            Icon(
                imageVector = Icons.Default.Style,
                contentDescription = null
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = style,
                onValueChange = onStyleChange,
                label = { Text(stringResource(R.string.work_record_label_style)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Style, contentDescription = null) },
                trailingIcon = {
                    Box {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.work_record_content_description_style_history),
                            modifier = Modifier.clickable { expanded = true }
                        )
                        AppDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            if (styleList.isEmpty()) {
                                AppDropdownMenuItem(
                                    text = stringResource(R.string.work_record_style_history_empty),
                                    onClick = { expanded = false },
                                    enabled = false
                                )
                            } else {
                                styleList.forEach { styleItem ->
                                    AppDropdownMenuItem(
                                        text = styleItem.name,
                                        onClick = {
                                            onStyleSelected(styleItem.name)
                                            expanded = false
                                        },
                                        selected = style == styleItem.name
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(STYLE_INPUT_TEST_TAG),
                singleLine = true,
                shape = AppTextFieldShape,
                colors = appOutlinedTextFieldColors(),
                isError = styleError != null,
                supportingText = {
                    workRecordFieldErrorText(styleError)?.let { Text(text = it) }
                }
            )
        }
    }
}

@Composable
private fun ProcessItemFormCard(
    pageNumber: Int,
    totalPageCount: Int,
    workRecordDetails: WorkRecordDetails,
    processList: List<Process>,
    styleList: List<Style>,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    validationErrors: WorkRecordValidationErrors,
    showNavigationControls: Boolean,
    showBasicInfo: Boolean,
    showImagesInBasicInfo: Boolean,
    showTotalQuantityField: Boolean,
    sharedTotalQuantity: String?,
    onSharedTotalQuantityChange: ((String) -> Unit)?,
    canNavigateToPrevious: Boolean,
    canNavigateToNext: Boolean,
    onShowPrevious: () -> Unit,
    onShowNext: () -> Unit,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onValueChange: (WorkRecordDetails) -> Unit,
    onProcessSelected: (Process) -> Unit,
    onAddProcess: (String, Double, String) -> Unit,
    onUpdateProcess: (Process) -> Unit,
    onDeleteProcess: (Process) -> Unit,
    onAddColorEntryFromPreset: (String, String) -> Unit,
    onUpdateColorEntryQuantity: (String, String) -> Unit,
    onUpdateColorEntryDeficit: (String, String) -> Unit,
    onToggleColorEntryDeficitResolved: (String) -> Unit,
    onUpdateColorEntryColorCode: (String, String) -> Unit,
    onRemoveColorEntry: (String) -> Unit,
    onManageColorPresetsClick: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth(), emphasized = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showNavigationControls) {
                    AppIconActionButton(
                        onClick = onShowPrevious,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.work_record_group_previous_existing),
                        enabled = canNavigateToPrevious
                    )
                }
                Text(
                    text = stringResource(R.string.work_record_group_item_title, pageNumber, totalPageCount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showNavigationControls) {
                    AppIconActionButton(
                        onClick = onShowNext,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.work_record_group_next_existing),
                        enabled = canNavigateToNext
                    )
                }
            }
            if (canRemove) {
                AppActionChip(
                    text = stringResource(R.string.common_delete),
                    onClick = onRemove,
                    emphasized = true,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }

    if (showBasicInfo) {
        BasicInfoSectionCard(
            workRecordDetails = workRecordDetails,
            onValueChange = onValueChange,
            showImages = showImagesInBasicInfo
        )
    }

    ProcessStyleSectionCard(
        workRecordDetails = workRecordDetails,
        processList = processList,
        styleList = styleList,
        onValueChange = onValueChange,
        onProcessSelected = onProcessSelected,
        onStyleSelected = {},
        onAddProcess = onAddProcess,
        onUpdateProcess = onUpdateProcess,
        onDeleteProcess = onDeleteProcess,
        styleError = null,
        processError = validationErrors.processName,
        showStyleField = false,
        showTotalQuantityField = showTotalQuantityField,
        totalQuantityValue = sharedTotalQuantity ?: workRecordDetails.totalQuantity,
        onTotalQuantityChange = { value ->
            if (onSharedTotalQuantityChange != null) {
                onSharedTotalQuantityChange(value)
            } else {
                onValueChange(workRecordDetails.copy(totalQuantity = value))
            }
        }
    )

    ColorDetailSectionCard(
        workRecordDetails = workRecordDetails,
        colorGroups = colorGroups,
        colorPresets = colorPresets,
        onAddColorEntryFromPreset = onAddColorEntryFromPreset,
        onUpdateColorEntryQuantity = onUpdateColorEntryQuantity,
        onUpdateColorEntryDeficit = onUpdateColorEntryDeficit,
        onToggleColorEntryDeficitResolved = onToggleColorEntryDeficitResolved,
        onUpdateColorEntryColorCode = onUpdateColorEntryColorCode,
        onRemoveColorEntry = onRemoveColorEntry,
        onManageColorPresetsClick = onManageColorPresetsClick
    )

    RecordAmountSectionCard(
        workRecordDetails = workRecordDetails,
        onValueChange = onValueChange,
        quantityError = validationErrors.quantity,
        unitPriceError = validationErrors.unitPrice
    )

    RecordTimeSectionCard(
        workRecordDetails = workRecordDetails,
        onValueChange = onValueChange
    )

    RecordRemarkSectionCard(
        workRecordDetails = workRecordDetails,
        onValueChange = onValueChange
    )
}
