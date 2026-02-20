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
import com.example.processrecord.ui.viewmodel.BackupViewModel
import com.example.processrecord.ui.viewmodel.StyleManageViewModel
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
                processRecordApplication().container.workRecordRepository
            )
        }
        initializer {
            WorkRecordStatsViewModel(processRecordApplication().container.workRecordRepository)
        }
        initializer {
            BackupViewModel(processRecordApplication().container.backupManager)
        }
        initializer {
            StyleManageViewModel(processRecordApplication().container.styleRepository)
        }
    }
}

fun CreationExtras.processRecordApplication(): ProcessRecordApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProcessRecordApplication)
