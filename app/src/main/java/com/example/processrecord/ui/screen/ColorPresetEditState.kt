package com.example.processrecord.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset

class ColorPresetEditState internal constructor() {
    val collapsedState = mutableStateMapOf<Long, Boolean>()

    var pendingDeletePreset by mutableStateOf<ColorPreset?>(null)
        private set

    var editingPreset by mutableStateOf<ColorPreset?>(null)
        private set

    var editPresetName by mutableStateOf("")
        private set

    var pendingDeleteGroup by mutableStateOf<ColorGroup?>(null)
        private set

    var editingGroup by mutableStateOf<ColorGroup?>(null)
        private set

    var editGroupName by mutableStateOf("")
        private set

    fun toggleGroupCollapsed(groupId: Long) {
        val current = collapsedState[groupId] ?: false
        collapsedState[groupId] = !current
    }

    fun requestDeletePreset(preset: ColorPreset) {
        pendingDeletePreset = preset
    }

    fun dismissDeletePreset() {
        pendingDeletePreset = null
    }

    fun startEditPreset(preset: ColorPreset) {
        editingPreset = preset
        editPresetName = preset.name
    }

    fun updateEditPresetName(name: String) {
        editPresetName = name
    }

    fun dismissEditPreset() {
        editingPreset = null
    }

    fun requestDeleteGroup(group: ColorGroup) {
        pendingDeleteGroup = group
    }

    fun dismissDeleteGroup() {
        pendingDeleteGroup = null
    }

    fun startEditGroup(group: ColorGroup) {
        editingGroup = group
        editGroupName = group.name
    }

    fun updateEditGroupName(name: String) {
        editGroupName = name
    }

    fun dismissEditGroup() {
        editingGroup = null
    }
}

@Composable
fun rememberColorPresetEditState(): ColorPresetEditState {
    return remember { ColorPresetEditState() }
}
