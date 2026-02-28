package com.example.processrecord.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.backup.BackupOperations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val messageType: BackupMessageType = BackupMessageType.Info,
    val restoreSuccess: Boolean = false
)

enum class BackupMessageType {
    Info,
    ExportError,
    ImportError
}

class BackupViewModel(
    private val backupManager: BackupOperations,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun getDefaultFileName(): String = backupManager.getDefaultBackupFileName()

    fun exportDatabase(uri: Uri) {
        exportDatabaseWith { backupManager.exportDatabase(uri) }
    }

    internal fun exportDatabaseWith(operation: suspend () -> Result<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                messageType = BackupMessageType.Info
            )
            try {
                val result = withContext(ioDispatcher) {
                    operation()
                }
                result.fold(
                    onSuccess = { msg ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = msg,
                            messageType = BackupMessageType.Info
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = e.message,
                            messageType = BackupMessageType.ExportError
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message,
                    messageType = BackupMessageType.ExportError
                )
            }
        }
    }

    fun importDatabase(uri: Uri) {
        importDatabaseWith { backupManager.importDatabase(uri) }
    }

    internal fun importDatabaseWith(operation: suspend () -> Result<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                messageType = BackupMessageType.Info,
                restoreSuccess = false
            )
            try {
                val result = withContext(ioDispatcher) {
                    operation()
                }
                result.fold(
                    onSuccess = { msg ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = msg,
                            messageType = BackupMessageType.Info,
                            restoreSuccess = true
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = e.message,
                            messageType = BackupMessageType.ImportError
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message,
                    messageType = BackupMessageType.ImportError
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            messageType = BackupMessageType.Info
        )
    }
}
