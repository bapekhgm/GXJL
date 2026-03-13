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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.color_picker_title),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                color = Color(
                                    red = red.toInt(),
                                    green = green.toInt(),
                                    blue = blue.toInt()
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                    )
                }

                Text(
                    text = hexValue,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(stringResource(R.string.color_picker_red, red.toInt()), color = MaterialTheme.colorScheme.primary)
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    valueRange = 0f..255f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    stringResource(R.string.color_picker_green, green.toInt()),
                    color = MaterialTheme.colorScheme.secondary
                )
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    valueRange = 0f..255f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    stringResource(R.string.color_picker_blue, blue.toInt()),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    valueRange = 0f..255f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                height = 44.dp
            )
            AppPrimaryButton(
                text = stringResource(R.string.common_save),
                onClick = { onConfirm(red, green, blue) },
                height = 44.dp
            )
        }
    )
}
