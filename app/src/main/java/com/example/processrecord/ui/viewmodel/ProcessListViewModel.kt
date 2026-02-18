package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.entity.Process
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProcessListViewModel(private val processRepository: ProcessRepository) : ViewModel() {
    val processListUiState: StateFlow<ProcessListUiState> =
        processRepository.getAllProcessesStream().map { ProcessListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProcessListUiState()
            )

    fun deleteProcess(process: Process) {
        viewModelScope.launch {
            processRepository.deleteProcess(process)
        }
    }
}

data class ProcessListUiState(val processList: List<Process> = listOf())
