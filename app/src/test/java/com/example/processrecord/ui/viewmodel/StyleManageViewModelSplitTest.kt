package com.example.processrecord.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class StyleManageViewModelSplitTest {

    @Test
    fun splitStyleNames_supportsMixedSeparators() {
        val input = "A\nB,C\uFF0CD\u3001E F\tG"
        assertEquals(listOf("A", "B", "C", "D", "E", "F", "G"), splitStyleNames(input))
    }

    @Test
    fun splitStyleNames_trimsAndDeduplicates() {
        val input = "  A , A \uFF0C A \u3001A  "
        assertEquals(listOf("A"), splitStyleNames(input))
    }
}
