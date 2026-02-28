package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup

@Composable
fun ColorPresetCreateSection(
    newName: String,
    groups: List<ColorGroup>,
    selectedGroupId: Long?,
    showGroupPicker: Boolean,
    previewHex: String,
    onNameChange: (String) -> Unit,
    onGroupPickerExpandedChange: (Boolean) -> Unit,
    onGroupSelected: (Long) -> Unit,
    onPickColorClick: () -> Unit,
    onSaveColorClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.color_preset_create_title),
        style = MaterialTheme.typography.titleMedium
    )

    OutlinedTextField(
        value = newName,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.color_preset_name_label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { onGroupPickerExpandedChange(true) },
            modifier = Modifier.fillMaxWidth(),
            enabled = groups.isNotEmpty()
        ) {
            val selectedGroupName = groups.firstOrNull { it.id == selectedGroupId }?.name
                ?: stringResource(R.string.color_group_select_placeholder)
            Text(stringResource(R.string.color_group_selected_value, selectedGroupName))
        }

        DropdownMenu(
            expanded = showGroupPicker,
            onDismissRequest = { onGroupPickerExpandedChange(false) }
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = {
                        onGroupSelected(group.id)
                        onGroupPickerExpandedChange(false)
                    }
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(parseColorOrDefault(previewHex), RoundedCornerShape(50))
        )
        Text(stringResource(R.string.color_preset_preview_value, previewHex))
    }

    OutlinedButton(
        onClick = onPickColorClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.color_preset_pick_color))
    }

    OutlinedButton(
        onClick = onSaveColorClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = newName.isNotBlank() && selectedGroupId != null
    ) {
        Text(stringResource(R.string.color_preset_save_color))
    }
}

@Composable
fun ColorGroupManageSection(
    newGroupName: String,
    onNewGroupNameChange: (String) -> Unit,
    onAddGroupClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.color_group_manage_title),
        style = MaterialTheme.typography.titleMedium
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = newGroupName,
            onValueChange = onNewGroupNameChange,
            label = { Text(stringResource(R.string.color_group_new_name_label)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedButton(
            onClick = onAddGroupClick,
            enabled = newGroupName.isNotBlank(),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(stringResource(R.string.color_group_add_button))
        }
    }
}
