package com.example.processrecord.ui.viewmodel

import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.WorkRecord
import java.util.Calendar
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelExtensionsTest {

    @Test
    fun yuanToCents_roundsSafely_andSupportsCommaSeparator() {
        assertEquals(29L, yuanToCents("0.29"))
        assertEquals(30L, yuanToCents("0.295"))
        assertEquals(1250L, yuanToCents(" 12.50 "))
        assertEquals(1250L, yuanToCents("12,50"))
        assertEquals(0L, yuanToCents(" "))
        assertEquals(0L, yuanToCents("invalid"))
    }

    @Test
    fun toWorkRecord_normalizesDateAndAmount() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 15, 18, 45, 12)
            set(Calendar.MILLISECOND, 333)
        }
        val inputDate = calendar.timeInMillis

        val details = WorkRecordDetails(
            processName = "process-a",
            style = "S001",
            unitPrice = "0,29",
            quantity = "3",
            date = inputDate
        )
        val record = details.toWorkRecord()

        val expectedDate = Calendar.getInstance().apply {
            timeInMillis = inputDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(29L, record.unitPrice)
        assertEquals(87L, record.amount)
        assertEquals(expectedDate, record.date)
    }

    @Test
    fun toWorkRecordDetails_usesDotDecimalRegardlessOfLocale() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            val record = WorkRecord(
                id = 5L,
                processId = 9L,
                processName = "sewing",
                style = "X-1",
                unitPrice = 123L,
                quantity = 4L,
                amount = 492L,
                date = 1_700_000_000_000L
            )

            val details = record.toWorkRecordDetails()

            assertEquals("1.23", details.unitPrice)
            assertEquals("4.92", details.amount)
            assertEquals("4", details.quantity)
            assertEquals("0", details.totalQuantity)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun processMappings_keepFieldsAndHandleInvalidPrice() {
        val process = Process(
            id = 3L,
            name = "cutting",
            defaultPrice = 2.5,
            unit = "piece",
            isActive = false
        )
        val detailsFromProcess = process.toProcessDetails()
        assertEquals("2.5", detailsFromProcess.defaultPrice)
        assertEquals("cutting", detailsFromProcess.name)
        assertEquals("piece", detailsFromProcess.unit)
        assertEquals(false, detailsFromProcess.isActive)

        val invalidPriceDetails = ProcessDetails(
            id = 7L,
            name = "locking",
            defaultPrice = "abc",
            unit = "meter",
            isActive = true
        )
        val mappedProcess = invalidPriceDetails.toProcess()
        assertEquals(7L, mappedProcess.id)
        assertEquals("locking", mappedProcess.name)
        assertEquals("meter", mappedProcess.unit)
        assertEquals(true, mappedProcess.isActive)
        assertEquals(0.0, mappedProcess.defaultPrice, 0.0)

        val commaPriceDetails = ProcessDetails(
            id = 8L,
            name = "hemming",
            defaultPrice = "2,5",
            unit = "piece",
            isActive = true
        )
        val commaMapped = commaPriceDetails.toProcess()
        assertEquals(8L, commaMapped.id)
        assertEquals(2.5, commaMapped.defaultPrice, 0.0)

        val blankUnitDetails = ProcessDetails(
            id = 9L,
            name = "pressing",
            defaultPrice = "",
            unit = "   ",
            isActive = true
        )
        val blankUnitMapped = blankUnitDetails.toProcess()
        assertEquals(9L, blankUnitMapped.id)
        assertEquals(0.0, blankUnitMapped.defaultPrice, 0.0)
        assertEquals(DEFAULT_PROCESS_UNIT, blankUnitMapped.unit)
    }
}
