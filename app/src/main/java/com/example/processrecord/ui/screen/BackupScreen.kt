package com.example.processrecord.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.component.AppTopBar
import com.example.processrecord.ui.component.ChromeIconButton
import com.example.processrecord.ui.viewmodel.BackupMessageType
import com.example.processrecord.ui.viewmodel.BackupViewModel
import kotlinx.coroutines.delay

@Composable
fun BackupScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { viewModel.exportDatabase(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importDatabase(it) }
    }

    var showImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.restoreSuccess) {
        if (uiState.restoreSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.backup_restore_success_restarting),
                Toast.LENGTH_SHORT
            ).show()
            delay(500)
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            if (!uiState.restoreSuccess) {
                val displayMessage = when (uiState.messageType) {
                    BackupMessageType.Info -> msg
                    BackupMessageType.ExportError -> context.getString(
                        R.string.backup_export_failed_message,
                        msg.ifBlank { context.getString(R.string.common_unknown_error) }
                    )

                    BackupMessageType.ImportError -> context.getString(
                        R.string.backup_import_failed_message,
                        msg.ifBlank { context.getString(R.string.common_unknown_error) }
                    )
                }
                Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show()
            }
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.backup_title),
                subtitle = stringResource(R.string.backup_subtitle),
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "backup_actions") {
                ElevatedSectionCard(gradientBackground = true) {
                    Text(
                        text = stringResource(R.string.backup_heading),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.backup_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
                    AppPrimaryButton(
                        text = stringResource(R.string.backup_export_button),
                        onClick = { exportLauncher.launch(viewModel.getDefaultFileName()) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))
                    AppSecondaryButton(
                        text = stringResource(R.string.backup_import_button),
                        onClick = { showImportDialog = true },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.isLoading) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }

            item(key = "backup_warning") {
                AccentCard(accentColor = MaterialTheme.colorScheme.tertiary) {
                    Text(
                        text = stringResource(R.string.backup_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        AppDialogScaffold(
            onDismissRequest = { showImportDialog = false },
            title = stringResource(R.string.backup_confirm_restore_title),
            supportingText = stringResource(R.string.backup_confirm_restore_message),
            actions = {
                AppSecondaryButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = { showImportDialog = false },
                    height = 44.dp
                )
                AppPrimaryButton(
                    text = stringResource(R.string.backup_confirm_restore_button),
                    onClick = {
                        showImportDialog = false
                        importLauncher.launch(arrayOf("*/*"))
                    },
                    height = 44.dp
                )
            }
        )
    }
}
