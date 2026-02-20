package com.example.processrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.ProcessDetails
import com.example.processrecord.ui.viewmodel.ProcessEntryViewModel
import com.example.processrecord.ui.viewmodel.ProcessUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessEntryScreen(
    viewModel: ProcessEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val title = if (viewModel.processUiState.processDetails.id == 0L) "添加工序" else "编辑工序"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
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
                    viewModel.saveProcess()
                    navigateBack()
                }
            },
            onDeleteClick = {
                coroutineScope.launch {
                    viewModel.deleteProcess()
                    navigateBack()
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
            title = { Text("确认删除") },
            text = { Text("确定要删除工序「${processUiState.processDetails.name}」吗？删除后关联的记录将解除工序关联。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
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
                Text(if (processUiState.processDetails.id == 0L) "保存" else "更新")
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
                    Text("删除")
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
        OutlinedTextField(
            value = processDetails.name,
            onValueChange = { onValueChange(processDetails.copy(name = it)) },
            label = { Text("工序名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = processDetails.defaultPrice,
            onValueChange = { onValueChange(processDetails.copy(defaultPrice = it)) },
            label = { Text("默认单价") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = processDetails.unit,
            onValueChange = { onValueChange(processDetails.copy(unit = it)) },
            label = { Text("单位 (如: 件, 小时)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
