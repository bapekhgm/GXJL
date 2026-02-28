package com.example.processrecord.ui.screen

import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPresetEditStateTest {

    @Test
    fun toggleGroupCollapsed_flipsBetweenTrueAndFalse() {
        val state = ColorPresetEditState()

        state.toggleGroupCollapsed(7L)
        assertTrue(state.collapsedState[7L] == true)

        state.toggleGroupCollapsed(7L)
        assertFalse(state.collapsedState[7L] == true)
    }

    @Test
    fun presetEditAndDeleteState_updatesAndClears() {
        val state = ColorPresetEditState()
        val preset = ColorPreset(id = 2L, name = "Red", hexValue = "#FF0000", groupId = 1L)

        state.requestDeletePreset(preset)
        assertEquals(preset, state.pendingDeletePreset)
        state.dismissDeletePreset()
        assertNull(state.pendingDeletePreset)

        state.startEditPreset(preset)
        assertEquals(preset, state.editingPreset)
        assertEquals("Red", state.editPresetName)
        state.updateEditPresetName("Ruby")
        assertEquals("Ruby", state.editPresetName)
        state.dismissEditPreset()
        assertNull(state.editingPreset)
    }

    @Test
    fun groupEditAndDeleteState_updatesAndClears() {
        val state = ColorPresetEditState()
        val group = ColorGroup(id = 3L, name = "Warm")

        state.requestDeleteGroup(group)
        assertEquals(group, state.pendingDeleteGroup)
        state.dismissDeleteGroup()
        assertNull(state.pendingDeleteGroup)

        state.startEditGroup(group)
        assertEquals(group, state.editingGroup)
        assertEquals("Warm", state.editGroupName)
        state.updateEditGroupName("Warm Tone")
        assertEquals("Warm Tone", state.editGroupName)
        state.dismissEditGroup()
        assertNull(state.editingGroup)
    }
}
