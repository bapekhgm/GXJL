package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset

@Composable
fun DeleteColorPresetDialog(
    preset: ColorPreset?,
    onDismiss: () -> Unit,
    onConfirm: (ColorPreset) -> Unit
) {
    if (preset == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_preset_delete_title)) },
        text = {
            Text(
                stringResource(
                    R.string.color_preset_delete_message,
                    preset.name
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(preset) }) {
                Text(stringResource(R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun EditColorPresetDialog(
    preset: ColorPreset?,
    editName: String,
    editHex: String,
    onEditNameChange: (String) -> Unit,
    onPickColorClick: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ColorPreset, String, String) -> Unit
) {
    if (preset == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_preset_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = onEditNameChange,
                    label = { Text(stringResource(R.string.color_preset_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(parseColorOrDefault(editHex), RoundedCornerShape(50))
                    )
                    Text(stringResource(R.string.color_preset_preview_value, editHex))
                }
                OutlinedButton(
                    onClick = onPickColorClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.color_preset_pick_color))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(preset, editName, editHex) },
                enabled = editName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun EditColorGroupDialog(
    group: ColorGroup?,
    editGroupName: String,
    onEditGroupNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ColorGroup, String) -> Unit
) {
    if (group == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_group_edit_title)) },
        text = {
            OutlinedTextField(
                value = editGroupName,
                onValueChange = onEditGroupNameChange,
                label = { Text(stringResource(R.string.color_group_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(group, editGroupName) },
                enabled = editGroupName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun DeleteColorGroupDialog(
    group: ColorGroup?,
    onDismiss: () -> Unit,
    onConfirm: (ColorGroup) -> Unit
) {
    if (group == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_group_delete_title)) },
        text = {
            Text(
                stringResource(
                    R.string.color_group_delete_message,
                    group.name
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(group) }) {
                Text(stringResource(R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
