package com.example.processrecord.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPresetCreateStateTest {

    @Test
    fun newPresetFields_updateAndClear() {
        val state = ColorPresetCreateState()

        state.updateNewName("Red")
        state.selectGroup(9L)
        state.setGroupPickerVisible(true)

        assertEquals("Red", state.newName)
        assertEquals(9L, state.selectedGroupId)
        assertTrue(state.showGroupPicker)

        state.clearNewName()
        state.setGroupPickerVisible(false)
        assertEquals("", state.newName)
        assertFalse(state.showGroupPicker)
    }

    @Test
    fun newGroupName_updateAndClear() {
        val state = ColorPresetCreateState()

        assertEquals("", state.newGroupName)
        assertNull(state.selectedGroupId)

        state.updateNewGroupName("Warm")
        assertEquals("Warm", state.newGroupName)

        state.clearNewGroupName()
        assertEquals("", state.newGroupName)
    }
}
