package com.example.processrecord.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordDateTimeUtilsTest {

    @Test
    fun calculateDurationParts_returnsNullForInvalidInput() {
        assertNull(calculateDurationParts(startTime = 0L, endTime = 1000L))
        assertNull(calculateDurationParts(startTime = 1000L, endTime = 1000L))
        assertNull(calculateDurationParts(startTime = 2000L, endTime = 1000L))
    }

    @Test
    fun calculateDurationParts_splitsDaysHoursAndMinutes() {
        val start = 1_000L
        val end = start + (2L * 86_400_000L) + (3L * 3_600_000L) + (15L * 60_000L) + 59_000L

        val parts = calculateDurationParts(startTime = start, endTime = end)

        assertEquals(DurationParts(days = 2L, hours = 3L, minutes = 15L), parts)
    }

    @Test
    fun calculateDurationParts_handlesDurationsUnderOneHour() {
        val start = 10_000L
        val end = start + (45L * 60_000L) + 1_000L

        val parts = calculateDurationParts(startTime = start, endTime = end)

        assertEquals(DurationParts(days = 0L, hours = 0L, minutes = 45L), parts)
    }
}
