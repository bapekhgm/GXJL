package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.processrecord.R
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppDangerButton
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.component.EnhancedTextField

import com.example.processrecord.ui.component.SmartNumberField
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.viewmodel.ProcessEntryViewModel
import com.example.processrecord.ui.viewmodel.ProcessUiState
import com.example.processrecord.ui.viewmodel.ProcessDetails
import com.example.processrecord.ui.viewmodel.ProcessAlreadyExistsException
import com.example.processrecord.ui.viewmodel.InvalidProcessInputException
import com.example.processrecord.ui.viewmodel.ProcessNotFoundException

@Composable
fun ProcessEntryScreen(
    viewModel: ProcessEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val title = if (viewModel.processUiState.processDetails.id == 0L) {
        stringResource(R.string.process_entry_title_add)
    } else {
        stringResource(R.string.process_entry_title_edit)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = title,
                subtitle = stringResource(
                    if (viewModel.processUiState.processDetails.id == 0L) {
                        R.string.process_entry_subtitle_add
                    } else {
                        R.string.process_entry_subtitle_edit
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
        ProcessEntryBody(
            processUiState = viewModel.processUiState,
            onProcessValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    val result = viewModel.saveProcess()
                    result.fold(
                        onSuccess = { navigateBack() },
                        onFailure = { error ->
                            val message = when (error) {
                                is ProcessAlreadyExistsException -> context.getString(
                                    R.string.process_exists_message,
                                    error.processName
                                )
                                is InvalidProcessInputException -> context.getString(R.string.process_input_invalid)
                                else -> context.getString(R.string.process_save_failed)
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
                    val result = viewModel.deleteProcess()
                    result.fold(
                        onSuccess = { navigateBack() },
                        onFailure = { error ->
                            val message = when (error) {
                                is ProcessNotFoundException -> context.getString(R.string.process_not_found)
                                else -> context.getString(R.string.process_delete_failed)
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
fun ProcessEntryBody(
    processUiState: ProcessUiState,
    onProcessValueChange: (ProcessDetails) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val details = processUiState.processDetails
    val saveButtonText = if (details.id == 0L) {
        stringResource(R.string.common_save)
    } else {
        stringResource(R.string.common_update)
    }

    if (showDeleteDialog) {
        ProcessDeleteConfirmDialog(
            name = details.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            }
        )
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProcessInputForm(
            processDetails = details,
            onValueChange = onProcessValueChange
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppPrimaryButton(
                text = saveButtonText,
                onClick = onSaveClick,
                enabled = processUiState.isEntryValid,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.CheckCircle
            )
            if (details.id != 0L) {
                AppDangerButton(
                    text = stringResource(R.string.common_delete),
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Delete
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ProcessInputForm(
    processDetails: ProcessDetails,
    onValueChange: (ProcessDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultUnit = stringResource(R.string.process_default_unit_value)

    SectionCard(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.process_list_title)) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EnhancedTextField(
                    value = processDetails.name,
                    onValueChange = { onValueChange(processDetails.copy(name = it)) },
                    label = { Text(stringResource(R.string.process_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ProcessSuffixSelector(
                    name = processDetails.name,
                    onNameChange = { onValueChange(processDetails.copy(name = it)) }
                )
            }

            SmartNumberField(
                value = processDetails.defaultPrice,
                onValueChange = { onValueChange(processDetails.copy(defaultPrice = it)) },
                label = stringResource(R.string.process_default_price_label),
                placeholder = "0.00",
                allowDecimal = true
            )

            EnhancedTextField(
                value = processDetails.unit,
                onValueChange = { onValueChange(processDetails.copy(unit = it)) },
                label = { Text(stringResource(R.string.process_unit_label)) },
                placeholder = {
                    Text(
                        text = defaultUnit,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ProcessDeleteConfirmDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.process_delete_confirm_title),
        supportingText = stringResource(R.string.process_delete_confirm_message, name),
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                height = 44.dp
            )
            AppDangerButton(
                text = stringResource(R.string.common_delete),
                onClick = onConfirm,
                height = 44.dp
            )
        }
    )
}
