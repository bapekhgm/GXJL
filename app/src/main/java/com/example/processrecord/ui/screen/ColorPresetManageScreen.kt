package com.example.processrecord.ui.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.R
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.ColorManageOperationNotice
import com.example.processrecord.ui.viewmodel.ColorGroupAlreadyExistsException
import com.example.processrecord.ui.viewmodel.ColorPresetAlreadyExistsException
import com.example.processrecord.ui.viewmodel.ColorOperationFailedException
import com.example.processrecord.ui.viewmodel.InvalidColorGroupInputException
import com.example.processrecord.ui.viewmodel.InvalidColorPresetInputException
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel

@Composable
fun ColorPresetManageScreen(
    navigateBack: () -> Unit,
    viewModel: WorkRecordEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val presets by viewModel.colorPresets.collectAsState()
    val groups by viewModel.colorGroups.collectAsState()
    val colorManageOperationError = viewModel.colorManageOperationError
    val colorManageOperationNotice = viewModel.colorManageOperationNotice
    val createState = rememberColorPresetCreateState()
    val createColorPicker = rememberColorPickerController()
    val editState = rememberColorPresetEditState()
    val editColorPicker = rememberColorPickerController()

    LaunchedEffect(colorManageOperationError) {
        val error = colorManageOperationError ?: return@LaunchedEffect
        val message = when (error) {
            is InvalidColorPresetInputException -> context.getString(R.string.color_preset_input_invalid)
            is ColorPresetAlreadyExistsException -> context.getString(
                R.string.color_preset_exists_message,
                error.presetName
            )
            is InvalidColorGroupInputException -> context.getString(R.string.color_group_input_invalid)
            is ColorGroupAlreadyExistsException -> context.getString(
                R.string.color_group_exists_message,
                error.groupName
            )
            is ColorOperationFailedException -> context.getString(R.string.color_manage_operation_failed)
            else -> context.getString(R.string.color_manage_operation_failed)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.consumeColorManageOperationError()
    }

    LaunchedEffect(colorManageOperationNotice) {
        val notice = colorManageOperationNotice ?: return@LaunchedEffect
        val message = when (notice) {
            is ColorManageOperationNotice.PresetAdded -> context.getString(
                R.string.color_preset_add_success,
                notice.name
            )
            is ColorManageOperationNotice.PresetUpdated -> context.getString(
                R.string.color_preset_update_success,
                notice.name
            )
            is ColorManageOperationNotice.PresetDeleted -> context.getString(
                R.string.color_preset_delete_success,
                notice.name
            )
            is ColorManageOperationNotice.GroupAdded -> context.getString(
                R.string.color_group_add_success,
                notice.name
            )
            is ColorManageOperationNotice.GroupUpdated -> context.getString(
                R.string.color_group_update_success,
                notice.name
            )
            is ColorManageOperationNotice.GroupDeleted -> context.getString(
                R.string.color_group_delete_success,
                notice.name
            )
        }
        when (notice) {
            is ColorManageOperationNotice.PresetAdded -> createState.clearNewName()
            is ColorManageOperationNotice.GroupAdded -> createState.clearNewGroupName()
            is ColorManageOperationNotice.PresetUpdated -> editState.dismissEditPreset()
            is ColorManageOperationNotice.GroupUpdated -> editState.dismissEditGroup()
            else -> Unit
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.consumeColorManageOperationNotice()
    }

    ColorPresetManageLayout(navigateBack = navigateBack) {
        ColorPresetManageContent(
            groups = groups,
            presets = presets,
            createState = createState,
            createColorPicker = createColorPicker,
            editState = editState,
            editColorPicker = editColorPicker,
            onAddColorPreset = { name, hex, groupId ->
                viewModel.addCustomColorPreset(name, hex, groupId)
            },
            onAddColorGroup = { name ->
                viewModel.addColorGroup(name)
            }
        )
    }

    ColorPresetOverlays(
        editState = editState,
        createColorPicker = createColorPicker,
        editColorPicker = editColorPicker,
        onDeletePreset = viewModel::deleteColorPreset,
        onUpdatePreset = { preset ->
            viewModel.updateColorPreset(preset)
        },
        onUpdateGroup = { group ->
            viewModel.updateColorGroup(group)
        },
        onDeleteGroup = viewModel::deleteColorGroup
    )
}
