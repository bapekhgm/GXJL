package com.example.processrecord.ui.screen

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorPresetColorUtilsTest {

    @Test
    fun rgbToHex_clampsOutOfRangeValues() {
        assertEquals("#00FF10", rgbToHex(-1, 500, 16))
    }

    @Test
    fun hexToRgb_parsesHashAndNonHashInputs() {
        assertEquals(Triple(10, 160, 255), hexToRgb("#0AA0FF"))
        assertEquals(Triple(10, 160, 255), hexToRgb("0AA0FF"))
    }

    @Test
    fun hexToRgb_returnsDefaultWhenInvalid() {
        assertEquals(Triple(158, 158, 158), hexToRgb("#GGGGGG"))
        assertEquals(Triple(158, 158, 158), hexToRgb("#123"))
    }

    @Test
    fun parseColorOrDefault_usesParsedColorOrFallback() {
        assertEquals(Color(10, 20, 30), parseColorOrDefault("#0A141E"))
        assertEquals(Color(158, 158, 158), parseColorOrDefault("not-a-color"))
    }
}
