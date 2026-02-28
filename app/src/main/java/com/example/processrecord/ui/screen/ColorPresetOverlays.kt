package com.example.processrecord.ui.screen

import androidx.compose.runtime.Composable
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset

@Composable
fun ColorPresetOverlays(
    editState: ColorPresetEditState,
    createColorPicker: ColorPickerController,
    editColorPicker: ColorPickerController,
    onDeletePreset: (ColorPreset) -> Unit,
    onUpdatePreset: (ColorPreset) -> Boolean,
    onUpdateGroup: (ColorGroup) -> Boolean,
    onDeleteGroup: (ColorGroup) -> Unit
) {
    DeleteColorPresetDialog(
        preset = editState.pendingDeletePreset,
        onDismiss = editState::dismissDeletePreset,
        onConfirm = { preset ->
            onDeletePreset(preset)
            editState.dismissDeletePreset()
        }
    )

    val editHex = editColorPicker.hex
    EditColorPresetDialog(
        preset = editState.editingPreset,
        editName = editState.editPresetName,
        editHex = editHex,
        onEditNameChange = editState::updateEditPresetName,
        onPickColorClick = { editColorPicker.show() },
        onDismiss = editState::dismissEditPreset,
        onConfirm = { preset, name, hex ->
            onUpdatePreset(
                preset.copy(
                    name = name,
                    hexValue = hex
                )
            )
        }
    )

    EditColorGroupDialog(
        group = editState.editingGroup,
        editGroupName = editState.editGroupName,
        onEditGroupNameChange = editState::updateEditGroupName,
        onDismiss = editState::dismissEditGroup,
        onConfirm = { group, name ->
            onUpdateGroup(group.copy(name = name))
        }
    )

    DeleteColorGroupDialog(
        group = editState.pendingDeleteGroup,
        onDismiss = editState::dismissDeleteGroup,
        onConfirm = { group ->
            onDeleteGroup(group)
            editState.dismissDeleteGroup()
        }
    )

    ColorPickerDialogHost(controller = createColorPicker)
    ColorPickerDialogHost(controller = editColorPicker)
}
