package com.example.processrecord

import android.app.Application
import com.example.processrecord.data.AppContainer

class ProcessRecordApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
