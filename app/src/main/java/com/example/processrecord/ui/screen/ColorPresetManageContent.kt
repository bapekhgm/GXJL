package com.example.processrecord.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.viewmodel.suggestHexByName

fun LazyListScope.ColorPresetManageContent(
    groups: List<ColorGroup>,
    presetsByGroupId: Map<Long, List<ColorPreset>>,
    createState: ColorPresetCreateState,
    createColorPicker: ColorPickerController,
    editState: ColorPresetEditState,
    editColorPicker: ColorPickerController,
    onAddColorPreset: (name: String, hex: String, groupId: Long) -> Boolean,
    onAddColorGroup: (name: String) -> Boolean
) {
    val previewHex = createColorPicker.hex

    item(key = "color_preset_create") {
        ColorPresetCreateSection(
            newName = createState.newName,
            groups = groups,
            selectedGroupId = createState.selectedGroupId,
            showGroupPicker = createState.showGroupPicker,
            previewHex = previewHex,
            onNameChange = {
                createState.updateNewName(it)
                suggestHexByName(it)?.let { hex ->
                    createColorPicker.updateFromHex(hex)
                }
            },
            onGroupPickerExpandedChange = createState::setGroupPickerVisible,
            onGroupSelected = createState::selectGroup,
            onPickColorClick = { createColorPicker.show() },
            onSaveColorClick = {
                createState.selectedGroupId?.let { groupId ->
                    onAddColorPreset(createState.newName, previewHex, groupId)
                }
            }
        )
    }

    item(key = "color_group_manage") {
        ColorGroupManageSection(
            newGroupName = createState.newGroupName,
            onNewGroupNameChange = createState::updateNewGroupName,
            onAddGroupClick = {
                onAddColorGroup(createState.newGroupName)
            }
        )
    }

    item(key = "color_preset_saved_info") {
        AccentCard(accentColor = MaterialTheme.colorScheme.secondary) {
            Text(
                text = stringResource(R.string.color_preset_saved_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = stringResource(R.string.color_preset_saved_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    ColorPresetGroupListSection(
        groups = groups,
        presetsByGroupId = presetsByGroupId,
        collapsedState = editState.collapsedState,
        onToggleGroupCollapsed = editState::toggleGroupCollapsed,
        onEditGroupClick = editState::startEditGroup,
        onDeleteGroupClick = editState::requestDeleteGroup,
        onEditPresetClick = { preset ->
            editState.startEditPreset(preset)
            editColorPicker.updateFromHex(preset.hexValue)
            editColorPicker.show()
        },
        onDeletePresetClick = editState::requestDeletePreset
    )

    item(key = "color_preset_bottom_spacing") {
        Spacer(modifier = Modifier.size(16.dp))
    }
}
