package com.example.processrecord.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.launch

class ProcessAlreadyExistsException(val processName: String) : IllegalArgumentException()
class InvalidProcessInputException : IllegalArgumentException()
class ProcessNotFoundException : NoSuchElementException()

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
        processUiState = ProcessUiState(
            processDetails = processDetails,
            isEntryValid = validateInput(processDetails)
        )
    }

    private fun validateInput(uiState: ProcessDetails = processUiState.processDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && isValidPrice(defaultPrice)
        }
    }

    private fun isValidPrice(input: String): Boolean {
        val normalized = normalizeDecimalInput(input)
        if (normalized.isEmpty()) return true
        val parsed = normalized.toDoubleOrNull() ?: return false
        return parsed.isFinite() && parsed >= 0.0
    }

    suspend fun saveProcess(): Result<Unit> {
        if (!validateInput()) {
            return Result.failure(InvalidProcessInputException())
        }
        return runCatching {
            val normalized = processUiState.processDetails.copy(
                name = processUiState.processDetails.name.trim(),
                unit = normalizeProcessUnit(processUiState.processDetails.unit)
            )
            val existing = processRepository.getProcessByName(normalized.name)
            if (processId != null) {
                if (existing != null && existing.id != processId) {
                    throw ProcessAlreadyExistsException(normalized.name)
                }
                processRepository.updateProcess(
                    normalized.toProcess().copy(
                        id = processId,
                        isActive = true
                    )
                )
            } else {
                when {
                    existing == null -> processRepository.insertProcess(
                        normalized.toProcess().copy(isActive = true)
                    )

                    existing.isActive -> throw ProcessAlreadyExistsException(normalized.name)

                    else -> processRepository.updateProcess(
                        normalized.toProcess().copy(
                            id = existing.id,
                            isActive = true
                        )
                    )
                }
            }
            Unit
        }
    }

    suspend fun deleteProcess(): Result<Unit> {
        val targetProcessId = processId ?: return Result.failure(ProcessNotFoundException())
        return runCatching {
            processRepository.deleteProcess(processUiState.processDetails.toProcess().copy(id = targetProcessId))
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
    val unit: String = "",
    val isActive: Boolean = true
)
