package com.example.processrecord.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class ColorPresetCreateState internal constructor() {
    var newName by mutableStateOf("")
        private set

    var selectedGroupId by mutableStateOf<Long?>(null)
        private set

    var showGroupPicker by mutableStateOf(false)
        private set

    var newGroupName by mutableStateOf("")
        private set

    fun updateNewName(name: String) {
        newName = name
    }

    fun selectGroup(groupId: Long) {
        selectedGroupId = groupId
    }

    fun setGroupPickerVisible(visible: Boolean) {
        showGroupPicker = visible
    }

    fun clearNewName() {
        newName = ""
    }

    fun updateNewGroupName(name: String) {
        newGroupName = name
    }

    fun clearNewGroupName() {
        newGroupName = ""
    }
}

@Composable
fun rememberColorPresetCreateState(): ColorPresetCreateState {
    return remember { ColorPresetCreateState() }
}
