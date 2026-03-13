package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.data.entity.Style
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppActionChip
import com.example.processrecord.ui.component.AppDangerButton
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppFloatingButton
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSelectableRow
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.component.EnhancedTextField
import com.example.processrecord.ui.component.EmptyStateCard
import com.example.processrecord.ui.viewmodel.StyleManageViewModel
import com.example.processrecord.ui.viewmodel.StyleManageViewModel.AddStyleResult
@Composable
fun StyleManageScreen(
    navigateBack: () -> Unit,
    viewModel: StyleManageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val styleList by viewModel.styleList.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showBatchDialog by remember { mutableStateOf(false) }
    var pendingDeleteStyle by remember { mutableStateOf<Style?>(null) }

    if (showAddDialog) {
        var inputText by remember { mutableStateOf("") }
        AppDialogScaffold(
            onDismissRequest = { showAddDialog = false },
            title = stringResource(R.string.style_manage_add_single_title),
            content = {
                EnhancedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(stringResource(R.string.style_manage_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    autoFocus = true
                )
            },
            actions = {
                AppSecondaryButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = { showAddDialog = false },
                    height = 44.dp
                )
                AppPrimaryButton(
                    text = stringResource(R.string.style_manage_add_button),
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
                    },
                    height = 44.dp
                )
            }
        )
    }

    if (showBatchDialog) {
        var batchText by remember { mutableStateOf("") }
        AppDialogScaffold(
            onDismissRequest = { showBatchDialog = false },
            title = stringResource(R.string.style_manage_batch_title),
            content = {
                Column {
                    Text(
                        text = stringResource(R.string.style_manage_batch_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EnhancedTextField(
                        value = batchText,
                        onValueChange = { batchText = it },
                        label = { Text(stringResource(R.string.style_manage_batch_list_label)) },
                        singleLine = false,
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        autoFocus = true
                    )
                }
            },
            actions = {
                AppSecondaryButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = { showBatchDialog = false },
                    height = 44.dp
                )
                AppPrimaryButton(
                    text = stringResource(R.string.style_manage_add_button),
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
                    },
                    height = 44.dp
                )
            }
        )
    }

    pendingDeleteStyle?.let { style ->
        AppDialogScaffold(
            onDismissRequest = { pendingDeleteStyle = null },
            title = stringResource(R.string.style_manage_delete_confirm_title),
            supportingText = context.getString(R.string.style_manage_delete_confirm_message, style.name),
            actions = {
                AppSecondaryButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = { pendingDeleteStyle = null },
                    height = 44.dp
                )
                AppDangerButton(
                    text = stringResource(R.string.common_delete),
                    onClick = {
                        viewModel.deleteStyle(style)
                        Toast.makeText(
                            context,
                            context.getString(R.string.style_manage_delete_toast, style.name),
                            Toast.LENGTH_SHORT
                        ).show()
                        pendingDeleteStyle = null
                    },
                    height = 44.dp
                )
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.style_manage_title),
                subtitle = stringResource(R.string.style_manage_subtitle),
                navigationIcon = {
                    ChromeIconButton(
                        onClick = navigateBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back)
                    )
                },
                actions = {
                    AppActionChip(
                        text = stringResource(R.string.style_manage_batch_action),
                        onClick = { showBatchDialog = true }
                    )
                }
            )
        },
        floatingActionButton = {
            AppFloatingButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.style_manage_fab_add_content_description)
            )
        }
    ) { innerPadding ->
        if (styleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = stringResource(R.string.style_manage_title),
                    subtitle = stringResource(R.string.style_manage_empty),
                    icon = Icons.Default.Add,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(items = styleList, key = { it.id }) { style ->
                    StyleItem(
                        style = style,
                        onDelete = {
                            pendingDeleteStyle = style
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
    AppSelectableRow(
        title = style.name,
        modifier = Modifier.fillMaxWidth(),
        emphasized = true,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = style.name.take(1),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            AppIconActionButton(
                onClick = onDelete,
                icon = Icons.Default.Delete,
                contentDescription = stringResource(R.string.style_manage_delete_content_description),
                tint = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.24f),
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                size = 36.dp
            )
        }
    )
}
