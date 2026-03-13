package com.example.processrecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.processrecord.ui.component.AppBackdrop
import com.example.processrecord.ui.navigation.ProcessRecordNavHost
import com.example.processrecord.ui.theme.ProcessRecordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProcessRecordTheme {
                AppBackdrop(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    ProcessRecordNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
