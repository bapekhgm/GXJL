package com.example.processrecord.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.processrecord.ui.screen.BackupScreen
import com.example.processrecord.ui.screen.ColorPresetManageScreen
import com.example.processrecord.ui.screen.HomeScreen
import com.example.processrecord.ui.screen.ProcessEntryScreen
import com.example.processrecord.ui.screen.ProcessListScreen
import com.example.processrecord.ui.screen.StyleManageScreen
import com.example.processrecord.ui.screen.WorkRecordEntryScreen

enum class ProcessRecordScreen {
    Home,
    ProcessList,
    ProcessEntry,
    WorkRecordEntry,
    ColorPresetManage,
    StyleManage,
    Backup
}

@Composable
fun ProcessRecordNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ProcessRecordScreen.Home.name,
        modifier = modifier
    ) {
        composable(route = ProcessRecordScreen.Home.name) {
            HomeScreen(
                navigateToRecordAdd = {
                    navController.navigate(ProcessRecordScreen.WorkRecordEntry.name)
                },
                navigateToGroupEntry = { entryGroupId, initialRecordId ->
                    navController.navigate(
                        buildWorkRecordEntryRoute(
                            appendToGroupId = entryGroupId,
                            initialRecordId = initialRecordId
                        )
                    )
                },
                navigateToRecordEdit = { recordId ->
                    navController.navigate(buildWorkRecordEntryRoute(recordId = recordId))
                },
                navigateToProcessList = { navController.navigate(ProcessRecordScreen.ProcessList.name) },
                navigateToStyleManage = { navController.navigate(ProcessRecordScreen.StyleManage.name) },
                navigateToBackup = { navController.navigate(ProcessRecordScreen.Backup.name) }
            )
        }
        composable(route = ProcessRecordScreen.ProcessList.name) {
            ProcessListScreen(
                navigateBack = { navController.popBackStack() },
                navigateToProcessEntry = { navController.navigate(ProcessRecordScreen.ProcessEntry.name) },
                navigateToProcessEdit = { processId -> navController.navigate("${ProcessRecordScreen.ProcessEntry.name}?processId=$processId") }
            )
        }
        composable(
            route = "${ProcessRecordScreen.ProcessEntry.name}?processId={processId}",
            arguments = listOf(androidx.navigation.navArgument("processId") { 
                nullable = true 
                defaultValue = null
            })
        ) {
            ProcessEntryScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "${ProcessRecordScreen.WorkRecordEntry.name}?recordId={recordId}&appendToGroupId={appendToGroupId}&initialRecordId={initialRecordId}",
            arguments = listOf(
                androidx.navigation.navArgument("recordId") {
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("appendToGroupId") {
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("initialRecordId") {
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            WorkRecordEntryScreen(
                navigateBack = { navController.popBackStack() },
                navigateToColorPresetManage = { navController.navigate(ProcessRecordScreen.ColorPresetManage.name) }
            )
        }
        composable(route = ProcessRecordScreen.ColorPresetManage.name) {
            ColorPresetManageScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ProcessRecordScreen.StyleManage.name) {
            StyleManageScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ProcessRecordScreen.Backup.name) {
            BackupScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}

private fun buildWorkRecordEntryRoute(
    recordId: Long? = null,
    appendToGroupId: String? = null,
    initialRecordId: Long? = null
): String {
    val params = buildList {
        recordId?.let { add("recordId=$it") }
        appendToGroupId?.let { add("appendToGroupId=${Uri.encode(it)}") }
        initialRecordId?.let { add("initialRecordId=$it") }
    }
    return if (params.isEmpty()) {
        ProcessRecordScreen.WorkRecordEntry.name
    } else {
        "${ProcessRecordScreen.WorkRecordEntry.name}?${params.joinToString("&")}"
    }
}
