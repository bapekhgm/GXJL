package com.example.processrecord.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.backup.DatabaseBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null
)

class BackupViewModel(
    private val backupManager: DatabaseBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun getDefaultFileName(): String = backupManager.getDefaultBackupFileName()

    fun exportDatabase(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                val result = withContext(Dispatchers.IO) {
                    backupManager.exportDatabase(uri)
                }
                result.fold(
                    onSuccess = { msg ->
                        _uiState.value = _uiState.value.copy(isLoading = false, message = msg)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, message = "备份失败：${e.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "备份失败：${e.message}")
            }
        }
    }

    fun importDatabase(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                val result = withContext(Dispatchers.IO) {
                    backupManager.importDatabase(uri)
                }
                result.fold(
                    onSuccess = { msg ->
                        _uiState.value = _uiState.value.copy(isLoading = false, message = msg)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, message = "恢复失败：${e.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "恢复失败：${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}