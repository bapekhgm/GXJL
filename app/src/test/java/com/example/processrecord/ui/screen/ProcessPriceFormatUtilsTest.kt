package com.example.processrecord.ui.screen

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProcessPriceFormatUtilsTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun formatProcessPriceValue_formatsIntegerWithoutDecimalPart() {
        assertEquals("12", formatProcessPriceValue(12.0))
    }

    @Test
    fun formatProcessPriceValue_keepsUpToTwoDecimalPlaces() {
        assertEquals("12.3", formatProcessPriceValue(12.3))
    }

    @Test
    fun formatProcessPriceValue_roundsToTwoDecimalPlaces() {
        assertEquals("12.35", formatProcessPriceValue(12.345))
    }

    @Test
    fun formatProcessPriceValue_removesTrailingZero() {
        assertEquals("12.5", formatProcessPriceValue(12.50))
    }

    @Test
    fun parseProcessPriceInput_parsesValidDecimal() {
        assertEquals(12.5, parseProcessPriceInput("12.5"))
        assertEquals(12.5, parseProcessPriceInput("12,5"))
        assertEquals(0.0, parseProcessPriceInput("0"))
    }

    @Test
    fun parseProcessPriceInput_returnsNull_forInvalidOrNegativeInput() {
        assertEquals(null, parseProcessPriceInput(""))
        assertEquals(null, parseProcessPriceInput("abc"))
        assertEquals(null, parseProcessPriceInput("-1"))
    }
}
