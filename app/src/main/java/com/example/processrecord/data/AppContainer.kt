package com.example.processrecord.data

import android.app.Application

class AppContainer(private val context: Application) {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val processRepository: ProcessRepository by lazy {
        OfflineProcessRepository(database.processDao())
    }

    val workRecordRepository: WorkRecordRepository by lazy {
        OfflineWorkRecordRepository(
            database.workRecordDao(),
            database.workRecordImageDao(),
            database.workRecordColorItemDao(),
            database.colorPresetDao(),
            database.colorGroupDao()
        )
    }

    val styleRepository: StyleRepository by lazy {
        StyleRepository(database.styleDao())
    }
}
