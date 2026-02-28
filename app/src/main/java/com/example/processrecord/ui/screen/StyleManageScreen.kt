package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.Style
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.StyleManageViewModel
import com.example.processrecord.ui.viewmodel.StyleManageViewModel.AddStyleResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleManageScreen(
    navigateBack: () -> Unit,
    viewModel: StyleManageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val styleList by viewModel.styleList.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showBatchDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var inputText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.style_manage_add_single_title)) },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(stringResource(R.string.style_manage_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addStyle(inputText) { result ->
                            val message = when (result) {
                                AddStyleResult.EmptyName -> {
                                    context.getString(R.string.style_manage_add_error_empty)
                                }

                                is AddStyleResult.Duplicate -> {
                                    context.getString(
                                        R.string.style_manage_add_error_exists,
                                        result.name
                                    )
                                }

                                is AddStyleResult.Added -> {
                                    context.getString(
                                        R.string.style_manage_add_success,
                                        result.name
                                    )
                                }
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (result is AddStyleResult.Added) {
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.style_manage_add_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showBatchDialog) {
        var batchText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = { Text(stringResource(R.string.style_manage_batch_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.style_manage_batch_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = batchText,
                        onValueChange = { batchText = it },
                        label = { Text(stringResource(R.string.style_manage_batch_list_label)) },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.batchAddStyles(batchText) { added, skipped ->
                            val msg = if (skipped > 0) {
                                context.resources.getQuantityString(
                                    R.plurals.style_manage_batch_result_with_skipped,
                                    added,
                                    added,
                                    skipped
                                )
                            } else {
                                context.resources.getQuantityString(
                                    R.plurals.style_manage_batch_result_added_only,
                                    added,
                                    added
                                )
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            showBatchDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.style_manage_add_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.style_manage_title)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showBatchDialog = true }) {
                        Text(stringResource(R.string.style_manage_batch_action))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.style_manage_fab_add_content_description)
                )
            }
        }
    ) { innerPadding ->
        if (styleList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.style_manage_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = pluralStringResource(
                            R.plurals.style_manage_count,
                            styleList.size,
                            styleList.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(items = styleList, key = { it.id }) { style ->
                    StyleItem(
                        style = style,
                        onDelete = {
                            viewModel.deleteStyle(style)
                            Toast.makeText(
                                context,
                                context.getString(R.string.style_manage_delete_toast, style.name),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleItem(
    style: Style,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = style.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.style_manage_delete_content_description),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
