package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.component.AppDangerButton
import com.example.processrecord.ui.component.AppDialogScaffold
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.component.EnhancedTextField

@Composable
fun DeleteColorPresetDialog(
    preset: ColorPreset?,
    onDismiss: () -> Unit,
    onConfirm: (ColorPreset) -> Unit
) {
    if (preset == null) return

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.color_preset_delete_title),
        supportingText = stringResource(
            R.string.color_preset_delete_message,
            preset.name
        ),
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                height = 44.dp
            )
            AppDangerButton(
                text = stringResource(R.string.common_delete),
                onClick = { onConfirm(preset) },
                height = 44.dp
            )
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

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.color_preset_edit_title),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EnhancedTextField(
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
                            .size(28.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        parseColorOrDefault(editHex),
                                        parseColorOrDefault(editHex).copy(alpha = 0.72f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = stringResource(R.string.color_preset_preview_value, editHex),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                AppSecondaryButton(
                    text = stringResource(R.string.color_preset_pick_color),
                    onClick = onPickColorClick,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
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
                onClick = { onConfirm(preset, editName, editHex) },
                enabled = editName.isNotBlank(),
                height = 44.dp
            )
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

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.color_group_edit_title),
        content = {
            EnhancedTextField(
                value = editGroupName,
                onValueChange = onEditGroupNameChange,
                label = { Text(stringResource(R.string.color_group_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                height = 44.dp
            )
            AppPrimaryButton(
                text = stringResource(R.string.common_save),
                onClick = { onConfirm(group, editGroupName) },
                enabled = editGroupName.isNotBlank(),
                height = 44.dp
            )
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

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.color_group_delete_title),
        supportingText = stringResource(
            R.string.color_group_delete_message,
            group.name
        ),
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                height = 44.dp
            )
            AppDangerButton(
                text = stringResource(R.string.common_delete),
                onClick = { onConfirm(group) },
                height = 44.dp
            )
        }
    )
}
