package com.example.processrecord.ui.screen

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatProcessPriceValue(value: Double): String {
    val formatter = DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.getDefault()))
    return formatter.format(value)
}

fun parseProcessPriceInput(input: String): Double? {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return null
    val parsed = normalized.toDoubleOrNull() ?: return null
    if (!parsed.isFinite() || parsed < 0.0) return null
    return parsed
}
