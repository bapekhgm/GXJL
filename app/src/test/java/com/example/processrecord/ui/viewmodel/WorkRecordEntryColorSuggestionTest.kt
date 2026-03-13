package com.example.processrecord.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkRecordEntryColorSuggestionTest {

    @Test
    fun suggestHexByName_returnsExpectedHex_forChineseKeyword() {
        assertEquals("#F44336", suggestHexByName("酒红"))
        assertEquals("#1F3A5F", suggestHexByName("深蓝"))
        assertEquals("#212121", suggestHexByName("黑色"))
    }

    @Test
    fun suggestHexByName_returnsExpectedHex_forEnglishKeyword() {
        assertEquals("#F44336", suggestHexByName("red"))
        assertEquals("#6F4E37", suggestHexByName("coffee brown"))
        assertEquals("#E0E0E0", suggestHexByName("light gray"))
    }

    @Test
    fun suggestHexByName_returnsNull_forBlankOrUnknownText() {
        assertNull(suggestHexByName(""))
        assertNull(suggestHexByName("   "))
        assertNull(suggestHexByName("unknown-color"))
    }
}
