package com.example.processrecord.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.processrecord.data.entity.WorkRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OfflineWorkRecordRepositoryConflictTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineWorkRecordRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = OfflineWorkRecordRepository(
            workRecordDao = database.workRecordDao(),
            workRecordImageDao = database.workRecordImageDao(),
            workRecordColorItemDao = database.workRecordColorItemDao(),
            colorPresetDao = database.colorPresetDao(),
            colorGroupDao = database.colorGroupDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addColorGroup_throwsConflict_whenNameAlreadyExists() = runBlocking {
        val firstId = repository.addColorGroup("Group-A")
        try {
            repository.addColorGroup("Group-A")
            fail("Expected ColorGroupNameConflictException")
        } catch (_: ColorGroupNameConflictException) {
            // expected
        }

        val groups = database.colorGroupDao().getAllGroups().first()

        assertTrue(firstId > 0)
        assertEquals(1, groups.size)
        assertEquals("Group-A", groups.first().name)
    }

    @Test
    fun addColorGroup_throwsConflict_whenNameOnlyDiffersByCaseOrSpaces() = runBlocking {
        val firstId = repository.addColorGroup("Group-A")
        try {
            repository.addColorGroup(" group-a ")
            fail("Expected ColorGroupNameConflictException")
        } catch (_: ColorGroupNameConflictException) {
            // expected
        }

        val groups = database.colorGroupDao().getAllGroups().first()

        assertTrue(firstId > 0)
        assertEquals(1, groups.size)
        assertEquals("Group-A", groups.first().name)
    }

    @Test
    fun addColorPreset_throwsConflict_whenDuplicateName() = runBlocking {
        val groupId = repository.addColorGroup("Default")

        repository.addColorPreset("Red", "#FF0000", groupId)
        try {
            repository.addColorPreset("Red", "#00FF00", groupId)
            fail("Expected ColorPresetNameConflictException")
        } catch (_: ColorPresetNameConflictException) {
            // expected
        }

        val presets = database.colorPresetDao().getAllPresets().first()

        assertEquals(1, presets.size)
        assertEquals("Red", presets.first().name)
        assertEquals("#FF0000", presets.first().hexValue)
    }

    @Test
    fun updateColorPreset_throwsConflict_whenTargetNameExistsIgnoringCase() = runBlocking {
        val groupId = repository.addColorGroup("Default")
        repository.addColorPreset("Red", "#FF0000", groupId)
        repository.addColorPreset("Blue", "#0000FF", groupId)

        val presetsBefore = database.colorPresetDao().getAllPresets().first()
        val blue = presetsBefore.first { it.name == "Blue" }

        try {
            repository.updateColorPreset(blue.copy(name = " red "))
            fail("Expected ColorPresetNameConflictException")
        } catch (_: ColorPresetNameConflictException) {
            // expected
        }

        val presetsAfter = database.colorPresetDao().getAllPresets().first().sortedBy { it.name }
        assertEquals(listOf("Blue", "Red"), presetsAfter.map { it.name })
    }

    @Test
    fun updateColorGroup_throwsConflict_whenTargetNameExistsIgnoringCase() = runBlocking {
        val firstId = repository.addColorGroup("Warm")
        val secondId = repository.addColorGroup("Cool")

        try {
            repository.updateColorGroup(
                com.example.processrecord.data.entity.ColorGroup(
                    id = secondId,
                    name = " warm "
                )
            )
            fail("Expected ColorGroupNameConflictException")
        } catch (_: ColorGroupNameConflictException) {
            // expected
        }

        val groupsAfter = database.colorGroupDao().getAllGroups().first().sortedBy { it.id }
        assertEquals(firstId, groupsAfter[0].id)
        assertEquals("Warm", groupsAfter[0].name)
        assertEquals(secondId, groupsAfter[1].id)
        assertEquals("Cool", groupsAfter[1].name)
    }

    @Test
    fun deleteRecord_doesNotDeleteSharedImageUntilLastReferenceRemoved() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sharedFile = File.createTempFile("shared-image-", ".jpg", context.cacheDir)
        sharedFile.writeText("image")
        val sharedPath = sharedFile.absolutePath

        val recordA = WorkRecord(
            processId = null,
            processName = "P1",
            style = "S1",
            unitPrice = 100,
            quantity = 1,
            amount = 100,
            date = System.currentTimeMillis()
        )
        val recordB = recordA.copy(style = "S2")

        val idA = repository.insertRecordWithDetails(recordA, listOf(sharedPath), emptyList())
        val idB = repository.insertRecordWithDetails(recordB, listOf(sharedPath), emptyList())

        repository.deleteRecord(repository.getRecordStream(idA)!!)
        assertTrue(sharedFile.exists())

        repository.deleteRecord(repository.getRecordStream(idB)!!)
        assertFalse(sharedFile.exists())
    }
}
