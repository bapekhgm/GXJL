package com.example.processrecord.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun Divider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1f
        )
    }
}

@Composable
fun ClothingTagIcon(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        // Tag body
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.15f)
            lineTo(w * 0.55f, h * 0.15f)
            lineTo(w * 0.9f, h * 0.5f)
            lineTo(w * 0.55f, h * 0.85f)
            lineTo(w * 0.1f, h * 0.85f)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = 2f))
        // Hole
        drawCircle(
            color = tint,
            radius = w * 0.06f,
            center = Offset(w * 0.28f, h * 0.5f)
        )
    }
}