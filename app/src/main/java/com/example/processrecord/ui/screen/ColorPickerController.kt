package com.example.processrecord.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.processrecord.ui.component.ColorPickerDialog

class ColorPickerController internal constructor(
    initialRed: Float,
    initialGreen: Float,
    initialBlue: Float
) {
    var isVisible by mutableStateOf(false)
        private set

    var red by mutableStateOf(initialRed)
        private set

    var green by mutableStateOf(initialGreen)
        private set

    var blue by mutableStateOf(initialBlue)
        private set

    val hex: String
        get() = rgbToHex(red.toInt(), green.toInt(), blue.toInt())

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    fun updateFromHex(hex: String) {
        val (r, g, b) = hexToRgb(hex)
        red = r.toFloat()
        green = g.toFloat()
        blue = b.toFloat()
    }

    fun applySelection(newRed: Float, newGreen: Float, newBlue: Float) {
        red = newRed
        green = newGreen
        blue = newBlue
        isVisible = false
    }
}

@Composable
fun rememberColorPickerController(
    initialRed: Float = 244f,
    initialGreen: Float = 67f,
    initialBlue: Float = 54f
): ColorPickerController {
    return remember {
        ColorPickerController(
            initialRed = initialRed,
            initialGreen = initialGreen,
            initialBlue = initialBlue
        )
    }
}

@Composable
fun ColorPickerDialogHost(controller: ColorPickerController) {
    if (!controller.isVisible) return

    ColorPickerDialog(
        initialRed = controller.red,
        initialGreen = controller.green,
        initialBlue = controller.blue,
        onDismiss = { controller.hide() },
        onConfirm = { red, green, blue ->
            controller.applySelection(red, green, blue)
        }
    )
}
