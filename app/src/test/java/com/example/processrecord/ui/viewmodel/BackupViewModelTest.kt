package com.example.processrecord.ui.viewmodel

import android.net.Uri
import com.example.processrecord.data.backup.BackupOperations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun exportDatabase_setsInfoMessageOnSuccess() = runTest {
        val viewModel = BackupViewModel(
            backupManager = FakeBackupOperations(),
            ioDispatcher = mainDispatcherRule.testDispatcher
        )

        viewModel.exportDatabaseWith { Result.success("export-ok") }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("export-ok", state.message)
        assertEquals(BackupMessageType.Info, state.messageType)
        assertFalse(state.restoreSuccess)
    }

    @Test
    fun exportDatabase_setsExportErrorOnFailure() = runTest {
        val viewModel = BackupViewModel(
            backupManager = FakeBackupOperations(),
            ioDispatcher = mainDispatcherRule.testDispatcher
        )

        viewModel.exportDatabaseWith { Result.failure(Exception("export-fail")) }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("export-fail", state.message)
        assertEquals(BackupMessageType.ExportError, state.messageType)
        assertFalse(state.restoreSuccess)
    }

    @Test
    fun importDatabase_setsRestoreSuccessOnSuccess() = runTest {
        val viewModel = BackupViewModel(
            backupManager = FakeBackupOperations(),
            ioDispatcher = mainDispatcherRule.testDispatcher
        )

        viewModel.importDatabaseWith { Result.success("import-ok") }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("import-ok", state.message)
        assertEquals(BackupMessageType.Info, state.messageType)
        assertTrue(state.restoreSuccess)
    }

    @Test
    fun importDatabase_setsImportErrorOnFailure() = runTest {
        val viewModel = BackupViewModel(
            backupManager = FakeBackupOperations(),
            ioDispatcher = mainDispatcherRule.testDispatcher
        )

        viewModel.importDatabaseWith { Result.failure(Exception("import-fail")) }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("import-fail", state.message)
        assertEquals(BackupMessageType.ImportError, state.messageType)
        assertFalse(state.restoreSuccess)
    }

    @Test
    fun clearMessage_resetsMessageAndType() = runTest {
        val viewModel = BackupViewModel(
            backupManager = FakeBackupOperations(),
            ioDispatcher = mainDispatcherRule.testDispatcher
        )

        viewModel.exportDatabaseWith { Result.failure(Exception("temporary-error")) }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clearMessage()

        val state = viewModel.uiState.value
        assertNull(state.message)
        assertEquals(BackupMessageType.Info, state.messageType)
    }

    private class FakeBackupOperations : BackupOperations {
        override fun exportDatabase(destUri: Uri): Result<String> =
            error("Not used in this test suite")

        override fun importDatabase(sourceUri: Uri): Result<String> =
            error("Not used in this test suite")

        override fun getDefaultBackupFileName(): String = "backup.db"
    }
}
