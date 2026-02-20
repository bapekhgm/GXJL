package com.example.processrecord.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProcessEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val processRepository: ProcessRepository
) : ViewModel() {

    private val processId: Long? = savedStateHandle.get<String>("processId")?.toLongOrNull()

    var processUiState by mutableStateOf(ProcessUiState())
        private set

    init {
        if (processId != null) {
            viewModelScope.launch {
                processUiState = processRepository.getProcessStream(processId)
                    ?.toProcessDetails()
                    ?.let { ProcessUiState(processDetails = it, isEntryValid = true) }
                    ?: ProcessUiState()
            }
        }
    }

    fun updateUiState(processDetails: ProcessDetails) {
        processUiState = ProcessUiState(processDetails = processDetails, isEntryValid = validateInput(processDetails))
    }

    private fun validateInput(uiState: ProcessDetails = processUiState.processDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && defaultPrice.isNotBlank() && unit.isNotBlank()
        }
    }

    suspend fun saveProcess() {
        if (validateInput()) {
            if (processId != null) {
                processRepository.updateProcess(processUiState.processDetails.toProcess().copy(id = processId))
            } else {
                processRepository.insertProcess(processUiState.processDetails.toProcess())
            }
        }
    }

    suspend fun deleteProcess() {
        if (processId != null) {
            processRepository.deleteProcess(processUiState.processDetails.toProcess().copy(id = processId))
        }
    }
}

data class ProcessUiState(
    val processDetails: ProcessDetails = ProcessDetails(),
    val isEntryValid: Boolean = false
)

data class ProcessDetails(
    val id: Long = 0,
    val name: String = "",
    val defaultPrice: String = "",
    val unit: String = "件",
    val isActive: Boolean = true
)
