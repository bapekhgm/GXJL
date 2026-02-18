package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.AppViewModelProvider
import com.example.processrecord.ui.viewmodel.WorkRecordEntryViewModel
import com.example.processrecord.ui.viewmodel.suggestHexByName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPresetManageScreen(
    navigateBack: () -> Unit,
    viewModel: WorkRecordEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val presets by viewModel.colorPresets.collectAsState()
    val groups by viewModel.colorGroups.collectAsState()

    var newName by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var red by remember { mutableStateOf(244f) }
    var green by remember { mutableStateOf(67f) }
    var blue by remember { mutableStateOf(54f) }
    var pendingDelete by remember { mutableStateOf<ColorPreset?>(null) }
    var editingPreset by remember { mutableStateOf<ColorPreset?>(null) }
    var editName by remember { mutableStateOf("") }
    var editRed by remember { mutableStateOf(244f) }
    var editGreen by remember { mutableStateOf(67f) }
    var editBlue by remember { mutableStateOf(54f) }
    var showGroupPicker by remember { mutableStateOf(false) }

    var newGroupName by remember { mutableStateOf("") }
    var pendingDeleteGroup by remember { mutableStateOf<ColorGroup?>(null) }
    var editingGroup by remember { mutableStateOf<ColorGroup?>(null) }
    var editGroupName by remember { mutableStateOf("") }

    val collapsedState = remember { mutableStateMapOf<Long, Boolean>() }

    val previewHex = rgbToHex(red.toInt(), green.toInt(), blue.toInt())
    if (selectedGroupId == null && groups.isNotEmpty()) {
        selectedGroupId = groups.first().id
    }

    val groupedPresets = remember(presets, groups) { groupPresetsById(presets, groups) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("管理常用颜色") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("新增常用颜色", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = newName,
                onValueChange = {
                    newName = it
                    suggestHexByName(it)?.let { hex ->
                        val (r, g, b) = hexToRgb(hex)
                        red = r.toFloat()
                        green = g.toFloat()
                        blue = b.toFloat()
                    }
                },
                label = { Text("颜色名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showGroupPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedGroupName = groups.firstOrNull { it.id == selectedGroupId }?.name ?: "选择分组"
                    Text("分组：$selectedGroupName")
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = showGroupPicker,
                    onDismissRequest = { showGroupPicker = false }
                ) {
                    groups.forEach { group ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                selectedGroupId = group.id
                                showGroupPicker = false
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
                Text("预览: $previewHex")
            }

            Text("红色: ${red.toInt()}")
            Slider(value = red, onValueChange = { red = it }, valueRange = 0f..255f)
            Text("绿色: ${green.toInt()}")
            Slider(value = green, onValueChange = { green = it }, valueRange = 0f..255f)
            Text("蓝色: ${blue.toInt()}")
            Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..255f)

            OutlinedButton(
                onClick = {
                    val gid = selectedGroupId
                    if (gid != null) {
                        viewModel.addCustomColorPreset(newName, previewHex, gid)
                    }
                    newName = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = newName.isNotBlank() && selectedGroupId != null
            ) {
                Text("保存颜色")
            }

            Text("分组管理", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("新分组名") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedButton(
                    onClick = {
                        viewModel.addColorGroup(newGroupName)
                        newGroupName = ""
                    },
                    enabled = newGroupName.isNotBlank(),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("添加")
                }
            }

            Text("已保存的常用颜色", style = MaterialTheme.typography.titleMedium)

            groupedPresets.forEach { (groupName, items) ->
                val group = groups.firstOrNull { it.name == groupName }
                val isCollapsed = group?.let { collapsedState[it.id] } ?: false

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                group?.let { g ->
                                    collapsedState[g.id] = !(collapsedState[g.id] ?: false)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "展开收起")
                        }
                        Text(groupName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Row {
                        IconButton(
                            onClick = {
                                if (group != null) {
                                    editingGroup = group
                                    editGroupName = group.name
                                }
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "修改分组")
                        }
                        IconButton(
                            onClick = {
                                if (group != null && group.name != "自定义") {
                                    pendingDeleteGroup = group
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除分组")
                        }
                    }
                }

                if (isCollapsed) {
                    Spacer(modifier = Modifier.size(4.dp))
                    return@forEach
                }

                items.forEach { preset ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(parseColorOrDefault(preset.hexValue), RoundedCornerShape(50))
                            )
                            Text("${preset.name} (${preset.hexValue})")
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    editingPreset = preset
                                    editName = preset.name
                                    val (r, g, b) = hexToRgb(preset.hexValue)
                                    editRed = r.toFloat()
                                    editGreen = g.toFloat()
                                    editBlue = b.toFloat()
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "修改")
                            }
                            IconButton(onClick = { pendingDelete = preset }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定删除常用颜色“${pendingDelete?.name ?: ""}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete?.let { viewModel.deleteColorPreset(it) }
                        pendingDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (editingPreset != null) {
        val editHex = rgbToHex(editRed.toInt(), editGreen.toInt(), editBlue.toInt())
        AlertDialog(
            onDismissRequest = { editingPreset = null },
            title = { Text("修改颜色") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                            suggestHexByName(it)?.let { hex ->
                                val (r, g, b) = hexToRgb(hex)
                                editRed = r.toFloat()
                                editGreen = g.toFloat()
                                editBlue = b.toFloat()
                            }
                        },
                        label = { Text("颜色名称") },
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
                        Text("预览: $editHex")
                    }
                    Text("红色: ${editRed.toInt()}")
                    Slider(value = editRed, onValueChange = { editRed = it }, valueRange = 0f..255f)
                    Text("绿色: ${editGreen.toInt()}")
                    Slider(value = editGreen, onValueChange = { editGreen = it }, valueRange = 0f..255f)
                    Text("蓝色: ${editBlue.toInt()}")
                    Slider(value = editBlue, onValueChange = { editBlue = it }, valueRange = 0f..255f)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingPreset?.let {
                            viewModel.updateColorPreset(
                                it.copy(
                                    name = editName,
                                    hexValue = editHex
                                )
                            )
                        }
                        editingPreset = null
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPreset = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (editingGroup != null) {
        AlertDialog(
            onDismissRequest = { editingGroup = null },
            title = { Text("修改分组") },
            text = {
                OutlinedTextField(
                    value = editGroupName,
                    onValueChange = { editGroupName = it },
                    label = { Text("分组名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingGroup?.let { viewModel.updateColorGroup(it.copy(name = editGroupName)) }
                        editingGroup = null
                    },
                    enabled = editGroupName.isNotBlank()
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingGroup = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (pendingDeleteGroup != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteGroup = null },
            title = { Text("确认删除分组") },
            text = { Text("确定删除分组“${pendingDeleteGroup?.name ?: ""}”吗？该分组颜色将移动到“自定义”。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteGroup?.let { viewModel.deleteColorGroup(it) }
                        pendingDeleteGroup = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteGroup = null }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun parseColorOrDefault(hexValue: String): androidx.compose.ui.graphics.Color {
    return try {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hexValue))
    } catch (_: Exception) {
        androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }
}

private fun rgbToHex(red: Int, green: Int, blue: Int): String {
    val r = red.coerceIn(0, 255)
    val g = green.coerceIn(0, 255)
    val b = blue.coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

private fun hexToRgb(hex: String): Triple<Int, Int, Int> {
    return try {
        val color = android.graphics.Color.parseColor(hex)
        Triple(android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color))
    } catch (_: Exception) {
        Triple(158, 158, 158)
    }
}

private fun groupPresetsById(
    presets: List<ColorPreset>,
    groups: List<ColorGroup>
): List<Pair<String, List<ColorPreset>>> {
    val groupById = groups.associateBy { it.id }
    val ordered = groups.sortedBy { it.sortOrder }
    return ordered.mapNotNull { group ->
        val items = presets.filter { it.groupId == group.id }.sortedBy { it.sortOrder }
        if (items.isEmpty()) null else group.name to items
    }
}
