package com.example.processrecord.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessSuffixSelectorTest {

    @Test
    fun applyProcessSuffix_appendsSuffixWhenMissing() {
        assertEquals("Pocket*2", applyProcessSuffix("Pocket", "*2"))
    }

    @Test
    fun applyProcessSuffix_replacesExistingSuffix() {
        assertEquals("Pocket*3", applyProcessSuffix("Pocket*1", "*3"))
    }

    @Test
    fun applyProcessSuffix_handlesBlankBaseName() {
        assertEquals("*4", applyProcessSuffix("", "*4"))
    }

    @Test
    fun clearProcessSuffix_removesNumericSuffix() {
        assertEquals("Pocket", clearProcessSuffix("Pocket*12"))
    }

    @Test
    fun clearProcessSuffix_keepsNameWhenNoSuffix() {
        assertEquals("Pocket", clearProcessSuffix("Pocket"))
    }
}
