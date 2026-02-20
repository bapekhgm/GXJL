package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 导出备份
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.exportDatabase(context, it) }
    }

    // 导入备份
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importDatabase(context, it) }
    }

    var showImportDialog by remember { mutableStateOf(false) }

    // 显示消息
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据备份") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "备份与恢复",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "将数据库导出为文件保存，或从备份文件恢复数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val fileName = viewModel.getDefaultFileName()
                    exportLauncher.launch(fileName)
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("导出备份")
            }

            OutlinedButton(
                onClick = { showImportDialog = true },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("从文件恢复")
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "提示：恢复数据会覆盖当前所有记录，请谨慎操作。恢复后需重启应用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    // 恢复确认弹窗
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("确认恢复") },
            text = { Text("恢复将覆盖当前所有数据，确定要继续吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importLauncher.launch(arrayOf("*/*"))
                }) { Text("选择文件并恢复") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("取消") }
            }
        )
    }
}