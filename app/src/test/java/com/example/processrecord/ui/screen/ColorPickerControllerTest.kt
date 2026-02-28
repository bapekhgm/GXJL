package com.example.processrecord.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPickerControllerTest {

    @Test
    fun hex_reflectsCurrentRgbValues() {
        val controller = ColorPickerController(
            initialRed = 10f,
            initialGreen = 160f,
            initialBlue = 255f
        )

        assertEquals("#0AA0FF", controller.hex)
    }

    @Test
    fun updateFromHex_updatesRgbValues() {
        val controller = ColorPickerController(
            initialRed = 0f,
            initialGreen = 0f,
            initialBlue = 0f
        )

        controller.updateFromHex("#112233")

        assertEquals(17f, controller.red)
        assertEquals(34f, controller.green)
        assertEquals(51f, controller.blue)
        assertEquals("#112233", controller.hex)
    }

    @Test
    fun visibilityAndSelection_workAsExpected() {
        val controller = ColorPickerController(
            initialRed = 0f,
            initialGreen = 0f,
            initialBlue = 0f
        )

        controller.show()
        assertTrue(controller.isVisible)

        controller.applySelection(newRed = 5f, newGreen = 6f, newBlue = 7f)
        assertFalse(controller.isVisible)
        assertEquals("#050607", controller.hex)

        controller.show()
        controller.hide()
        assertFalse(controller.isVisible)
    }
}
