package com.example.processrecord.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.StyleRepository
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class WorkRecordEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val workRecordRepository: WorkRecordRepository,
    private val processRepository: ProcessRepository,
    private val styleRepository: StyleRepository
) : ViewModel() {

    private val recordId: Long? = savedStateHandle.get<String>("recordId")?.toLongOrNull()
    private val copyFromId: Long? = savedStateHandle.get<String>("copyFromId")?.toLongOrNull()

    var workRecordUiState by mutableStateOf(WorkRecordUiState())
        private set

    val processList: StateFlow<List<Process>> =
        processRepository.getAllProcessesStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val styleList: StateFlow<List<Style>> =
        styleRepository.getAllStylesStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val colorPresets: StateFlow<List<ColorPreset>> =
        workRecordRepository.getColorPresetsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val colorGroups: StateFlow<List<ColorGroup>> =
        workRecordRepository.getColorGroupsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = defaultColorGroups()
            )

    init {
        viewModelScope.launch {
            val existingGroups = workRecordRepository.getColorGroupsStream().first()
            if (existingGroups.isEmpty()) {
                defaultColorGroups().forEach { group ->
                    workRecordRepository.addColorGroup(group.name)
                }
            }

            val groups = workRecordRepository.getColorGroupsStream().first()
            val groupMap = groups.associateBy { it.name }

            val existingPresets = workRecordRepository.getColorPresetsStream().first()
            if (existingPresets.isEmpty()) {
                defaultColorPresets().forEach { preset ->
                    val groupId = groupMap[preset.groupName]?.id ?: groupMap["自定义"]?.id ?: 0L
                    workRecordRepository.addColorPreset(preset.name, preset.hexValue, groupId)
                }
            }
        }

        if (recordId != null) {
            viewModelScope.launch {
                 val record = workRecordRepository.getRecordStream(recordId)
                 if (record != null) {
                     val images = workRecordRepository.getImagesForRecord(record.id)
                     val colorItems = workRecordRepository.getColorItemsForRecord(record.id)
                     val colorEntries = if (colorItems.isNotEmpty()) {
                         colorItems.map { ColorEntryUi(it.colorName, it.colorHex, formatQuantity(it.quantity)) }
                     } else {
                         parseLegacyColorEntries(record.color)
                     }
                     val details = record.toWorkRecordDetails().copy(
                         imagePaths = images,
                         colorEntries = colorEntries
                     )
                     workRecordUiState = WorkRecordUiState(workRecordDetails = details, isEntryValid = true)
                     if (colorEntries.isNotEmpty()) {
                         onColorEntriesChanged(colorEntries)
                     }
                 }
            }
        } else if (copyFromId != null) {
             viewModelScope.launch {
                 val record = workRecordRepository.getRecordStream(copyFromId)
                 if (record != null) {
                     val images = workRecordRepository.getImagesForRecord(record.id)
                     val colorItems = workRecordRepository.getColorItemsForRecord(record.id)
                     val colorEntries = if (colorItems.isNotEmpty()) {
                         colorItems.map { ColorEntryUi(it.colorName, it.colorHex, formatQuantity(it.quantity)) }
                     } else {
                         parseLegacyColorEntries(record.color)
                     }
                     val details = record.toWorkRecordDetails()
                         .copy(
                            id = 0, 
                            date = System.currentTimeMillis(),
                            startTime = 0,
                            endTime = 0,
                            imagePaths = images,
                            colorEntries = colorEntries
                         )
                     workRecordUiState = WorkRecordUiState(workRecordDetails = details, isEntryValid = true)
                     if (colorEntries.isNotEmpty()) {
                         onColorEntriesChanged(colorEntries)
                     }
                 }
            }
        }
    }

    fun updateUiState(recordDetails: WorkRecordDetails) {
        // 自动计算金额
        val quantity = recordDetails.quantity.toDoubleOrNull() ?: 0.0
        val unitPrice = recordDetails.unitPrice.toDoubleOrNull() ?: 0.0
        val amount = quantity * unitPrice
        
        workRecordUiState = WorkRecordUiState(
            workRecordDetails = recordDetails.copy(amount = String.format(Locale.getDefault(), "%.2f", amount)),
            isEntryValid = validateInput(recordDetails)
        )
    }
    
    fun onProcessSelected(process: Process) {
        val currentDetails = workRecordUiState.workRecordDetails
        val newDetails = currentDetails.copy(
            processId = process.id,
            processName = process.name,
            unitPrice = process.defaultPrice.toString()
        )
        updateUiState(newDetails)
    }

    fun onStyleSelected(styleName: String) {
        updateUiState(workRecordUiState.workRecordDetails.copy(style = styleName))
    }

    fun addColorEntryFromPreset(colorName: String, colorHex: String) {
        val existing = workRecordUiState.workRecordDetails.colorEntries
        if (existing.any { it.colorName == colorName }) return
        onColorEntriesChanged(existing + ColorEntryUi(colorName = colorName, colorHex = colorHex, quantity = ""))
    }

    fun updateColorEntryQuantity(colorName: String, quantity: String) {
        val updated = workRecordUiState.workRecordDetails.colorEntries.map {
            if (it.colorName == colorName) it.copy(quantity = quantity) else it
        }
        onColorEntriesChanged(updated)
    }

    fun removeColorEntry(colorName: String) {
        val updated = workRecordUiState.workRecordDetails.colorEntries.filterNot { it.colorName == colorName }
        onColorEntriesChanged(updated)
    }

    fun addCustomColorPreset(name: String, hexValue: String, groupId: Long) {
        if (name.isBlank()) return
        val normalizedHex = normalizeHex(hexValue)
        viewModelScope.launch {
            workRecordRepository.addColorPreset(name.trim(), normalizedHex, groupId)
        }
    }

    fun deleteColorPreset(preset: ColorPreset) {
        viewModelScope.launch {
            workRecordRepository.deleteColorPreset(preset)
        }
    }

    fun updateColorPreset(preset: ColorPreset) {
        viewModelScope.launch {
            workRecordRepository.updateColorPreset(
                preset.copy(
                    name = preset.name.trim(),
                    hexValue = normalizeHex(preset.hexValue)
                )
            )
        }
    }

    fun addColorGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            workRecordRepository.addColorGroup(name.trim())
        }
    }

    fun updateColorGroup(group: ColorGroup) {
        if (group.name.isBlank()) return
        viewModelScope.launch {
            workRecordRepository.updateColorGroup(group.copy(name = group.name.trim()))
        }
    }

    fun deleteColorGroup(group: ColorGroup) {
        viewModelScope.launch {
            workRecordRepository.deleteColorGroup(group)
        }
    }

    fun onColorEntriesChanged(entries: List<ColorEntryUi>) {
        val hasAnyQuantity = entries.any { it.quantity.toDoubleOrNull() != null && it.quantity.isNotBlank() }
        val colorSummary = buildColorSummary(entries)
        val newDetails = if (hasAnyQuantity) {
            val totalQuantity = entries.sumOf { it.quantity.toDoubleOrNull() ?: 0.0 }
            workRecordUiState.workRecordDetails.copy(
                colorEntries = entries,
                color = colorSummary,
                quantity = formatQuantity(totalQuantity)
            )
        } else {
            // 颜色明细都没填数量时，保留用户手动输入的数量
            workRecordUiState.workRecordDetails.copy(
                colorEntries = entries,
                color = colorSummary
            )
        }
        updateUiState(newDetails)
    }

    fun deleteStyle(styleName: String) {
        viewModelScope.launch {
            styleRepository.deleteStyleByName(styleName)
        }
    }

    fun addProcess(name: String, defaultPrice: Double, unit: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            processRepository.insertProcess(Process(name = name.trim(), defaultPrice = defaultPrice, unit = unit.trim()))
        }
    }

    fun updateProcess(process: Process) {
        viewModelScope.launch {
            processRepository.updateProcess(process)
            // 如果当前选中的就是这个工序，同步更新 UI
            if (workRecordUiState.workRecordDetails.processId == process.id) {
                updateUiState(workRecordUiState.workRecordDetails.copy(
                    processName = process.name,
                    unitPrice = process.defaultPrice.toString()
                ))
            }
        }
    }

    fun deleteProcess(process: Process) {
        viewModelScope.launch {
            processRepository.deleteProcess(process)
            // 如果当前选中的就是被删除的工序，清空选择
            if (workRecordUiState.workRecordDetails.processId == process.id) {
                updateUiState(workRecordUiState.workRecordDetails.copy(
                    processId = null,
                    processName = "",
                    unitPrice = ""
                ))
            }
        }
    }

    fun addNewStyle(styleName: String) {
        if (styleName.isNotBlank()) {
            viewModelScope.launch {
                // Check if exists not implemented in DAO for simplicity, just try insert (ignore conflict) or just insert
                // But better to check. For now, let's just insert.
                styleRepository.insertStyle(Style(name = styleName))
                onStyleSelected(styleName)
            }
        }
    }

    private fun validateInput(uiState: WorkRecordDetails = workRecordUiState.workRecordDetails): Boolean {
        return with(uiState) {
            style.isNotBlank() && 
            processName.isNotBlank() && 
            // quantity.isNotBlank() && // Quantity can be empty if just starting work
            // unitPrice.isNotBlank() &&
            date > 0
        }
    }

    suspend fun saveWorkRecord() {
        if (validateInput()) {
            val record = workRecordUiState.workRecordDetails.toWorkRecord()
            val images = workRecordUiState.workRecordDetails.imagePaths
            val colorItems = workRecordUiState.workRecordDetails.colorEntries.mapIndexed { index, entry ->
                com.example.processrecord.data.entity.WorkRecordColorItem(
                    workRecordId = 0, // 事务内部会自动填充正确的 workRecordId
                    colorName = entry.colorName,
                    colorHex = normalizeHex(entry.colorHex),
                    quantity = entry.quantity.toDoubleOrNull() ?: 0.0,
                    sortOrder = index
                )
            }

            // 自动保存新款号
            if (styleList.value.none { it.name == record.style }) {
                styleRepository.insertStyle(Style(name = record.style))
            }

            if (recordId != null) {
                // 更新：原子性更新主记录 + 图片 + 颜色明细
                workRecordRepository.updateRecordWithDetails(
                    record = record.copy(id = recordId),
                    images = images,
                    colorItems = colorItems
                )
            } else {
                // 新增：原子性插入主记录 + 图片 + 颜色明细
                workRecordRepository.insertRecordWithDetails(
                    record = record,
                    images = images,
                    colorItems = colorItems
                )
            }
        }
    }

    suspend fun deleteRecord() {
        if (recordId != null) {
             workRecordRepository.deleteRecord(workRecordUiState.workRecordDetails.toWorkRecord().copy(id = recordId))
        }
    }
}

data class WorkRecordUiState(
    val workRecordDetails: WorkRecordDetails = WorkRecordDetails(),
    val isEntryValid: Boolean = false
)

data class WorkRecordDetails(
    val id: Long = 0,
    val processId: Long? = null,
    val processName: String = "",
    val style: String = "",
    val unitPrice: String = "",
    val quantity: String = "",
    val amount: String = "0.00",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val remark: String = "",
    val totalQuantity: String = "",
    val serialNumber: String = "",
    val color: String = "",
    val colorEntries: List<ColorEntryUi> = emptyList(),
    val imagePaths: List<String> = emptyList(),
    val date: Long = System.currentTimeMillis()
)

data class ColorEntryUi(
    val colorName: String,
    val colorHex: String,
    val quantity: String
)

private fun formatQuantity(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.getDefault(), "%.2f", value)
}

private fun normalizeHex(hex: String): String {
    val value = hex.trim().uppercase(Locale.getDefault())
    if (!value.startsWith("#")) return "#9E9E9E"
    if (value.length == 7) return value
    return "#9E9E9E"
}

fun suggestHexByName(name: String): String? {
    val text = name.trim().lowercase(Locale.getDefault())
    if (text.isBlank()) return null
    return when {
        text.contains("红") -> "#F44336"
        text.contains("橙") -> "#FF9800"
        text.contains("黄") -> "#FFEB3B"
        text.contains("军绿") -> "#4B5320"
        text.contains("绿") -> "#4CAF50"
        text.contains("青") -> "#00BCD4"
        text.contains("藏蓝") || text.contains("深蓝") -> "#1F3A5F"
        text.contains("蓝") -> "#2196F3"
        text.contains("紫") -> "#9C27B0"
        text.contains("白") || text.contains("米") -> "#F5F5DC"
        text.contains("卡其") -> "#C3B091"
        text.contains("驼") -> "#B8860B"
        text.contains("咖啡") || text.contains("棕") -> "#6F4E37"
        text.contains("酒红") -> "#8B1A1A"
        text.contains("粉") -> "#F48FB1"
        text.contains("天蓝") -> "#87CEEB"
        text.contains("浅灰") -> "#E0E0E0"
        text.contains("深灰") -> "#616161"
        text.contains("灰") -> "#9E9E9E"
        text.contains("黑") -> "#212121"
        else -> null
    }
}

private fun buildColorSummary(entries: List<ColorEntryUi>): String {
    return entries.joinToString(" ") { entry ->
        val qty = entry.quantity.trim()
        if (qty.isBlank()) entry.colorName else "${entry.colorName}$qty"
    }
}

private fun parseLegacyColorEntries(text: String): List<ColorEntryUi> {
    if (text.isBlank()) return emptyList()
    val regex = """([\u4E00-\u9FA5A-Za-z]+)\s*(\d+(?:\.\d+)?)""".toRegex()
    val matched = regex.findAll(text).map {
        val name = it.groupValues[1]
        val qty = it.groupValues[2]
        ColorEntryUi(name, defaultHexByName(name), qty)
    }.toList()
    return if (matched.isNotEmpty()) matched else listOf(ColorEntryUi(text.trim(), "#9E9E9E", ""))
}

private fun defaultColorGroups(): List<ColorGroup> = listOf(
    ColorGroup(id = 1, name = "基础色", sortOrder = 1),
    ColorGroup(id = 2, name = "中性色", sortOrder = 2),
    ColorGroup(id = 3, name = "常见面料色", sortOrder = 3),
    ColorGroup(id = 4, name = "自定义", sortOrder = 9999)
)

private data class DefaultPresetSeed(
    val name: String,
    val hexValue: String,
    val groupName: String,
    val sortOrder: Int
)

private fun defaultColorPresets(): List<DefaultPresetSeed> = listOf(
    DefaultPresetSeed(name = "红色", hexValue = "#F44336", groupName = "基础色", sortOrder = 1),
    DefaultPresetSeed(name = "橙色", hexValue = "#FF9800", groupName = "基础色", sortOrder = 2),
    DefaultPresetSeed(name = "黄色", hexValue = "#FFEB3B", groupName = "基础色", sortOrder = 3),
    DefaultPresetSeed(name = "绿色", hexValue = "#4CAF50", groupName = "基础色", sortOrder = 4),
    DefaultPresetSeed(name = "青色", hexValue = "#00BCD4", groupName = "基础色", sortOrder = 5),
    DefaultPresetSeed(name = "蓝色", hexValue = "#2196F3", groupName = "基础色", sortOrder = 6),
    DefaultPresetSeed(name = "紫色", hexValue = "#9C27B0", groupName = "基础色", sortOrder = 7),
    DefaultPresetSeed(name = "黑色", hexValue = "#212121", groupName = "中性色", sortOrder = 8),
    DefaultPresetSeed(name = "深灰", hexValue = "#616161", groupName = "中性色", sortOrder = 9),
    DefaultPresetSeed(name = "灰色", hexValue = "#9E9E9E", groupName = "中性色", sortOrder = 10),
    DefaultPresetSeed(name = "浅灰", hexValue = "#E0E0E0", groupName = "中性色", sortOrder = 11),
    DefaultPresetSeed(name = "白色", hexValue = "#FFFFFF", groupName = "中性色", sortOrder = 12),
    DefaultPresetSeed(name = "米白", hexValue = "#F5F5DC", groupName = "常见面料色", sortOrder = 13),
    DefaultPresetSeed(name = "卡其", hexValue = "#C3B091", groupName = "常见面料色", sortOrder = 14),
    DefaultPresetSeed(name = "驼色", hexValue = "#B8860B", groupName = "常见面料色", sortOrder = 15),
    DefaultPresetSeed(name = "咖啡", hexValue = "#6F4E37", groupName = "常见面料色", sortOrder = 16),
    DefaultPresetSeed(name = "藏蓝", hexValue = "#1F3A5F", groupName = "常见面料色", sortOrder = 17),
    DefaultPresetSeed(name = "酒红", hexValue = "#8B1A1A", groupName = "常见面料色", sortOrder = 18),
    DefaultPresetSeed(name = "军绿", hexValue = "#4B5320", groupName = "常见面料色", sortOrder = 19),
    DefaultPresetSeed(name = "粉色", hexValue = "#F48FB1", groupName = "常见面料色", sortOrder = 20),
    DefaultPresetSeed(name = "天蓝", hexValue = "#87CEEB", groupName = "常见面料色", sortOrder = 21)
)

private fun defaultHexByName(name: String): String {
    val text = name.trim()
    return when {
        text.contains("红") -> "#F44336"
        text.contains("橙") -> "#FF9800"
        text.contains("黄") -> "#FFEB3B"
        text.contains("军绿") -> "#4B5320"
        text.contains("绿") -> "#4CAF50"
        text.contains("青") -> "#00BCD4"
        text.contains("藏蓝") || text.contains("深蓝") -> "#1F3A5F"
        text.contains("蓝") -> "#2196F3"
        text.contains("紫") -> "#9C27B0"
        text.contains("白") || text.contains("米") -> "#F5F5DC"
        text.contains("卡其") -> "#C3B091"
        text.contains("驼") -> "#B8860B"
        text.contains("咖啡") || text.contains("棕") -> "#6F4E37"
        text.contains("酒红") -> "#8B1A1A"
        text.contains("粉") -> "#F48FB1"
        text.contains("天蓝") -> "#87CEEB"
        text.contains("浅灰") -> "#E0E0E0"
        text.contains("深灰") -> "#616161"
        text.contains("灰") -> "#9E9E9E"
        text.contains("黑") -> "#212121"
        else -> "#9E9E9E"
    }
}

