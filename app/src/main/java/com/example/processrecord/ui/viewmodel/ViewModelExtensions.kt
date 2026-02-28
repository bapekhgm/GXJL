package com.example.processrecord.ui.viewmodel

import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.WorkRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

// Process extensions.
fun Process.toProcessDetails(): ProcessDetails = ProcessDetails(
    id = id,
    name = name,
    defaultPrice = defaultPrice.toString(),
    unit = unit,
    isActive = isActive
)

fun ProcessDetails.toProcess(): Process = Process(
    id = id,
    name = name,
    defaultPrice = normalizeDecimalInput(defaultPrice).toDoubleOrNull() ?: 0.0,
    unit = unit,
    isActive = isActive
)

// WorkRecord extensions.
// Database stores monetary values in cents; UI edits values in yuan.

/**
 * Convert a stored WorkRecord (cents) to editable WorkRecordDetails (yuan string).
 */
fun WorkRecord.toWorkRecordDetails(): WorkRecordDetails = WorkRecordDetails(
    id = id,
    processId = processId,
    processName = processName,
    style = style,
    unitPrice = centsToYuan(unitPrice),
    quantity = quantity.toString(),
    amount = centsToYuan(amount),
    startTime = startTime,
    endTime = endTime,
    remark = remark,
    totalQuantity = totalQuantity.toString(),
    serialNumber = serialNumber,
    color = color,
    date = date
)

/**
 * Convert editable WorkRecordDetails (yuan) to persisted WorkRecord (cents).
 * Integer storage avoids floating-point precision drift.
 */
fun WorkRecordDetails.toWorkRecord(): WorkRecord {
    val unitPriceInCents = yuanToCents(unitPrice)
    val quantityValue = quantity.toLongOrNull() ?: 0L
    val amountInCents = unitPriceInCents * quantityValue
    val totalQuantityValue = totalQuantity.toLongOrNull() ?: 0L
    val normalizedDate = normalizeToDayStart(date)

    return WorkRecord(
        id = id,
        processId = processId,
        processName = processName,
        style = style,
        unitPrice = unitPriceInCents,
        quantity = quantityValue,
        amount = amountInCents,
        startTime = startTime,
        endTime = endTime,
        remark = remark,
        totalQuantity = totalQuantityValue,
        serialNumber = serialNumber,
        color = color,
        date = normalizedDate
    )
}

/**
 * Convert cents to a fixed two-decimal yuan string using dot as separator.
 */
private fun centsToYuan(cents: Long): String {
    return BigDecimal.valueOf(cents)
        .movePointLeft(2)
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
}

/**
 * Convert a yuan input string to cents.
 * Supports both dot and comma decimal separator, e.g. "12.5" / "12,5" -> 1250.
 */
internal fun yuanToCents(yuanString: String): Long {
    val parsed = normalizeDecimalInput(yuanString)
    if (parsed.isEmpty()) return 0L
    return runCatching {
        BigDecimal(parsed)
            .setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrDefault(0L)
}

internal fun normalizeDecimalInput(input: String): String =
    input.trim().replace(',', '.')

private fun normalizeToDayStart(timestamp: Long): Long {
    if (timestamp <= 0L) return timestamp
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
