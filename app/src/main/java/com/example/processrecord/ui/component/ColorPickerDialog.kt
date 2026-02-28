package com.example.processrecord.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.processrecord.R

@Composable
fun ColorPickerDialog(
    initialRed: Float,
    initialGreen: Float,
    initialBlue: Float,
    onDismiss: () -> Unit,
    onConfirm: (red: Float, green: Float, blue: Float) -> Unit
) {
    var red by remember { mutableFloatStateOf(initialRed) }
    var green by remember { mutableFloatStateOf(initialGreen) }
    var blue by remember { mutableFloatStateOf(initialBlue) }

    val hexValue = String.format("#%02X%02X%02X", red.toInt(), green.toInt(), blue.toInt())

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.color_picker_title),
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(
                                red = red.toInt(),
                                green = green.toInt(),
                                blue = blue.toInt()
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }

            Text(
                text = hexValue,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(stringResource(R.string.color_picker_red, red.toInt()), color = Color.Red)
            Slider(
                value = red,
                onValueChange = { red = it },
                valueRange = 0f..255f,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                stringResource(R.string.color_picker_green, green.toInt()),
                color = Color(0xFF4CAF50)
            )
            Slider(
                value = green,
                onValueChange = { green = it },
                valueRange = 0f..255f,
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.color_picker_blue, blue.toInt()), color = Color.Blue)
            Slider(
                value = blue,
                onValueChange = { blue = it },
                valueRange = 0f..255f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
                TextButton(onClick = { onConfirm(red, green, blue) }) {
                    Text(stringResource(R.string.common_save))
                }
            }
        }
    }
}
