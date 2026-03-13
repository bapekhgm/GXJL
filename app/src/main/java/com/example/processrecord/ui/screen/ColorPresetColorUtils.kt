package com.example.processrecord.ui.screen

import androidx.compose.ui.graphics.Color
import java.util.Locale

private val DEFAULT_RGB = Triple(158, 158, 158)

fun parseColorOrDefault(hexValue: String): Color {
    val (red, green, blue) = hexToRgb(hexValue)
    return Color(red = red, green = green, blue = blue)
}

fun rgbToHex(red: Int, green: Int, blue: Int): String {
    val r = red.coerceIn(0, 255)
    val g = green.coerceIn(0, 255)
    val b = blue.coerceIn(0, 255)
    return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
}

fun hexToRgb(hex: String): Triple<Int, Int, Int> {
    val value = hex.trim().removePrefix("#")
    if (value.length != 6) return DEFAULT_RGB

    return runCatching {
        val red = value.substring(0, 2).toInt(16)
        val green = value.substring(2, 4).toInt(16)
        val blue = value.substring(4, 6).toInt(16)
        Triple(red, green, blue)
    }.getOrDefault(DEFAULT_RGB)
}
