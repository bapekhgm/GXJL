package com.example.processrecord.ui.screen

import com.example.processrecord.data.entity.ColorPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorDetailSectionCardSelectionTest {

    @Test
    fun toggleColorPresetSelection_addsPresetWhenNotSelected() {
        val selected = mutableListOf<ColorPreset>()
        val blue = ColorPreset(id = 1L, name = "蓝色", hexValue = "#2196F3")

        selected.toggleColorPresetSelection(blue)

        assertEquals(listOf(blue), selected)
    }

    @Test
    fun toggleColorPresetSelection_removesPresetWhenSelected() {
        val white = ColorPreset(id = 2L, name = "白色", hexValue = "#F5F5DC")
        val blue = ColorPreset(id = 1L, name = "蓝色", hexValue = "#2196F3")
        val selected = mutableListOf(white, blue)

        selected.toggleColorPresetSelection(blue)

        assertEquals(listOf(white), selected)
    }

    @Test
    fun toggleColorPresetSelection_removesDuplicateEntriesWithSameId() {
        val blue = ColorPreset(id = 1L, name = "蓝色", hexValue = "#2196F3")
        val duplicateBlue = blue.copy(hexValue = "#0000FF")
        val selected = mutableListOf(blue, duplicateBlue)

        selected.toggleColorPresetSelection(blue)

        assertTrue(selected.isEmpty())
    }
}
