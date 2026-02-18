package com.example.processrecord.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.processrecord.ProcessRecordApplication
import com.example.processrecord.ui.viewmodel.ProcessEntryViewModel
import com.example.processrecord.ui.viewmodel.ExportViewModel
import com.example.processrecord.ui.viewmodel.ProcessListViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordListViewModel
import com.example.processrecord.ui.viewmodel.WorkRecordStatsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ProcessEntryViewModel(
                this.createSavedStateHandle(),
                processRecordApplication().container.processRepository
            )
        }
        initializer {
            ExportViewModel(processRecordApplication().container.workRecordRepository)
        }
        initializer {
            ProcessListViewModel(processRecordApplication().container.processRepository)
        }
        initializer {
            WorkRecordEntryViewModel(
                this.createSavedStateHandle(),
                processRecordApplication().container.workRecordRepository,
                processRecordApplication().container.processRepository,
                processRecordApplication().container.styleRepository
            )
        }
        initializer {
            WorkRecordListViewModel(
                processRecordApplication().container.workRecordRepository,
                processRecordApplication().container.database.workRecordColorItemDao()
            )
        }
        initializer {
            WorkRecordStatsViewModel(processRecordApplication().container.workRecordRepository)
        }
    }
}

fun CreationExtras.processRecordApplication(): ProcessRecordApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProcessRecordApplication)
