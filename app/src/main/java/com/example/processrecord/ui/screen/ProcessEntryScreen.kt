package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.InvalidProcessInputException
import com.example.processrecord.ui.viewmodel.ProcessDetails
import com.example.processrecord.ui.viewmodel.ProcessAlreadyExistsException
import com.example.processrecord.ui.viewmodel.ProcessEntryViewModel
import com.example.processrecord.ui.viewmodel.ProcessNotFoundException
import com.example.processrecord.ui.viewmodel.ProcessUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.process_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.process_delete_confirm_message,
                        processUiState.processDetails.name
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProcessInputForm(
            processDetails = processUiState.processDetails,
            onValueChange = onProcessValueChange
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSaveClick,
                enabled = processUiState.isEntryValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (processUiState.processDetails.id == 0L) {
                        stringResource(R.string.common_save)
                    } else {
                        stringResource(R.string.common_update)
                    }
                )
            }
            if (processUiState.processDetails.id != 0L) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Text(stringResource(R.string.common_delete))
                }
            }
        }
    }
}

@Composable
fun ProcessInputForm(
    processDetails: ProcessDetails,
    onValueChange: (ProcessDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
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

        OutlinedTextField(
            value = processDetails.defaultPrice,
            onValueChange = { onValueChange(processDetails.copy(defaultPrice = it)) },
            label = { Text(stringResource(R.string.process_default_price_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = processDetails.unit,
            onValueChange = { onValueChange(processDetails.copy(unit = it)) },
            label = { Text(stringResource(R.string.process_unit_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
