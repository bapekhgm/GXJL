package com.example.processrecord.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.ColorGroupNameConflictException
import com.example.processrecord.data.ColorPresetNameConflictException
import com.example.processrecord.data.ProcessRepository
import com.example.processrecord.data.StyleRepository
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class ProcessOperationFailedException : IllegalStateException()
class InvalidWorkRecordInputException : IllegalArgumentException()
class WorkRecordNotFoundException : NoSuchElementException()
class ColorOperationFailedException : IllegalStateException()
class InvalidColorPresetInputException : IllegalArgumentException()
class ColorPresetAlreadyExistsException(val presetName: String) : IllegalArgumentException()
class InvalidColorGroupInputException : IllegalArgumentException()
class ColorGroupAlreadyExistsException(val groupName: String) : IllegalArgumentException()

sealed interface ColorManageOperationNotice {
    data class PresetAdded(val name: String) : ColorManageOperationNotice
    data class PresetUpdated(val name: String) : ColorManageOperationNotice
    data class PresetDeleted(val name: String) : ColorManageOperationNotice
    data class GroupAdded(val name: String) : ColorManageOperationNotice
    data class GroupUpdated(val name: String) : ColorManageOperationNotice
    data class GroupDeleted(val name: String) : ColorManageOperationNotice
}

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
    var processOperationError by mutableStateOf<Throwable?>(null)
        private set
    var colorManageOperationError by mutableStateOf<Throwable?>(null)
        private set
    var colorManageOperationNotice by mutableStateOf<ColorManageOperationNotice?>(null)
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
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    val colorGroups: StateFlow<List<ColorGroup>> =
        workRecordRepository.getColorGroupsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    init {
        when {
            recordId != null -> {
                viewModelScope.launch {
                    loadRecordDetails(sourceRecordId = recordId, asCopy = false)
                }
            }

            copyFromId != null -> {
                viewModelScope.launch {
                    loadRecordDetails(sourceRecordId = copyFromId, asCopy = true)
                }
            }
        }
    }

    private suspend fun loadRecordDetails(sourceRecordId: Long, asCopy: Boolean) {
        val record = workRecordRepository.getRecordStream(sourceRecordId) ?: return
        val images = workRecordRepository.getImagesForRecord(record.id)
        val colorItems = workRecordRepository.getColorItemsForRecord(record.id)
        val colorEntries = if (colorItems.isNotEmpty()) {
            colorItems.map {
                ColorEntryUi(
                    colorName = it.colorName,
                    colorHex = it.colorHex,
                    quantity = formatQuantity(it.quantity),
                    deficit = formatQuantity(it.deficit),
                    colorCode = it.colorCode
                )
            }
        } else {
            parseLegacyColorEntries(record.color)
        }

        val details = if (asCopy) {
            record.toWorkRecordDetails().copy(
                id = 0,
                date = System.currentTimeMillis(),
                startTime = 0,
                endTime = 0,
                imagePaths = images,
                colorEntries = colorEntries
            )
        } else {
            record.toWorkRecordDetails().copy(
                imagePaths = images,
                colorEntries = colorEntries
            )
        }
        workRecordUiState = WorkRecordUiState(workRecordDetails = details, isEntryValid = true)
        if (colorEntries.isNotEmpty()) {
            onColorEntriesChanged(colorEntries)
        }
    }

    fun updateUiState(recordDetails: WorkRecordDetails) {
        val quantity = recordDetails.quantity.toLongOrNull() ?: 0L
        val unitPriceCents = yuanToCents(recordDetails.unitPrice)
        val amountCents = quantity * unitPriceCents
        val amountYuan = amountCents / 100.0
        
        workRecordUiState = WorkRecordUiState(
            workRecordDetails = recordDetails.copy(amount = String.format(Locale.getDefault(), "%.2f", amountYuan)),
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

    fun consumeProcessOperationError() {
        processOperationError = null
    }

    fun consumeColorManageOperationError() {
        colorManageOperationError = null
    }

    fun consumeColorManageOperationNotice() {
        colorManageOperationNotice = null
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

    fun updateColorEntryDeficit(colorName: String, deficit: String) {
        val updated = workRecordUiState.workRecordDetails.colorEntries.map {
            if (it.colorName == colorName) it.copy(deficit = deficit) else it
        }
        onColorEntriesChanged(updated)
    }

    fun updateColorEntryColorCode(colorName: String, colorCode: String) {
        val updated = workRecordUiState.workRecordDetails.colorEntries.map {
            if (it.colorName == colorName) it.copy(colorCode = colorCode) else it
        }
        onColorEntriesChanged(updated)
    }

    fun removeColorEntry(colorName: String) {
        val updated = workRecordUiState.workRecordDetails.colorEntries.filterNot { it.colorName == colorName }
        onColorEntriesChanged(updated)
    }

    fun addCustomColorPreset(name: String, hexValue: String, groupId: Long): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            colorManageOperationError = InvalidColorPresetInputException()
            return false
        }
        val duplicate = colorPresets.value.firstOrNull {
            it.name.trim().equals(trimmedName, ignoreCase = true)
        }
        if (duplicate != null) {
            colorManageOperationError = ColorPresetAlreadyExistsException(trimmedName)
            return false
        }
        val normalizedHex = normalizeHex(hexValue)
        viewModelScope.launch {
            runCatching {
                workRecordRepository.addColorPreset(trimmedName, normalizedHex, groupId)
            }.onSuccess {
                colorManageOperationNotice = ColorManageOperationNotice.PresetAdded(trimmedName)
            }.onFailure { error ->
                colorManageOperationError = asColorOperationError(error)
            }
        }
        return true
    }

    fun deleteColorPreset(preset: ColorPreset) {
        viewModelScope.launch {
            runCatching {
                workRecordRepository.deleteColorPreset(preset)
            }.onSuccess {
                colorManageOperationNotice = ColorManageOperationNotice.PresetDeleted(preset.name)
            }.onFailure { error ->
                colorManageOperationError = asColorOperationError(error)
            }
        }
    }

    fun updateColorPreset(preset: ColorPreset): Boolean {
        val trimmedName = preset.name.trim()
        if (trimmedName.isBlank()) {
            colorManageOperationError = InvalidColorPresetInputException()
            return false
        }
        val duplicate = colorPresets.value.firstOrNull {
            it.id != preset.id && it.name.trim().equals(trimmedName, ignoreCase = true)
        }
        if (duplicate != null) {
            colorManageOperationError = ColorPresetAlreadyExistsException(trimmedName)
            return false
        }
        viewModelScope.launch {
            runCatching {
                workRecordRepository.updateColorPreset(
                    preset.copy(
                        name = trimmedName,
                        hexValue = normalizeHex(preset.hexValue)
                    )
                )
            }.onSuccess {
                colorManageOperationNotice = ColorManageOperationNotice.PresetUpdated(trimmedName)
            }.onFailure { error ->
                colorManageOperationError = asColorOperationError(error)
            }
        }
        return true
    }

    fun addColorGroup(name: String): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            colorManageOperationError = InvalidColorGroupInputException()
            return false
        }
        val duplicate = colorGroups.value.firstOrNull {
            it.name.trim().equals(trimmedName, ignoreCase = true)
        }
        if (duplicate != null) {
            colorManageOperationError = ColorGroupAlreadyExistsException(trimmedName)
            return false
        }
        viewModelScope.launch {
            runCatching {
                workRecordRepository.addColorGroup(trimmedName)
            }.onSuccess {
                colorManageOperationNotice = ColorManageOperationNotice.GroupAdded(trimmedName)
            }.onFailure { error ->
                colorManageOperationError = asColorOperationError(error)
            }
        }
        return true
    }

    fun updateColorGroup(group: ColorGroup): Boolean {
        val trimmedName = group.name.trim()
        if (trimmedName.isBlank()) {
            colorManageOperationError = InvalidColorGroupInputException()
            return false
        }
        val duplicate = colorGroups.value.firstOrNull {
            it.id != group.id && it.name.trim().equals(trimmedName, ignoreCase = true)
        }
        if (duplicate != null) {
            colorManageOperationError = ColorGroupAlreadyExistsException(trimmedName)
            return false
        }
        viewModelScope.launch {
            runCatching {
                workRecordRepository.updateColorGroup(group.copy(name = trimmedName))
            }.onSuccess {
                colorManageOperationNotice = ColorManageOperationNotice.GroupUpdated(trimmedName)
            }.onFailure { error ->
                colorManageOperationError = asColorOperationError(error)
            }
        }
        return true
    }

    fun deleteColorGroup(group: ColorGroup) {
        viewModelScope.launch {
            runCatching {
                workRecordRepository.deleteColorGroup(group)
            }.onSuccess {
                colorManageOperationNotice = ColorManageOperationNotice.GroupDeleted(group.name)
            }.onFailure { error ->
                colorManageOperationError = asColorOperationError(error)
            }
        }
    }

    fun onColorEntriesChanged(entries: List<ColorEntryUi>) {
        val hasAnyQuantity = entries.any { it.quantity.toLongOrNull() != null && it.quantity.isNotBlank() }
        val colorSummary = buildColorSummary(entries)
        val newDetails = if (hasAnyQuantity) {
            val totalQuantity = entries.sumOf { it.quantity.toLongOrNull() ?: 0L }
            workRecordUiState.workRecordDetails.copy(
                colorEntries = entries,
                color = colorSummary,
                quantity = totalQuantity.toString()
            )
        } else {
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
        val trimmedName = name.trim()
        val trimmedUnit = unit.trim()
        if (!isValidProcessInput(trimmedName, defaultPrice, trimmedUnit)) {
            processOperationError = InvalidProcessInputException()
            return
        }
        viewModelScope.launch {
            runCatching {
                val existing = processRepository.getProcessByName(trimmedName)
                val selected = when {
                    existing == null -> {
                        val insertedId = processRepository.insertProcess(
                            Process(
                                name = trimmedName,
                                defaultPrice = defaultPrice,
                                unit = trimmedUnit,
                                isActive = true
                            )
                        )
                        if (insertedId <= 0L) {
                            processRepository.getProcessByName(trimmedName)
                                ?: throw ProcessOperationFailedException()
                        } else {
                            Process(
                                id = insertedId,
                                name = trimmedName,
                                defaultPrice = defaultPrice,
                                unit = trimmedUnit,
                                isActive = true
                            )
                        }
                    }

                    existing.isActive -> existing

                    else -> {
                        val reactivated = existing.copy(
                            name = trimmedName,
                            defaultPrice = defaultPrice,
                            unit = trimmedUnit,
                            isActive = true
                        )
                        processRepository.updateProcess(reactivated)
                        reactivated
                    }
                }
                onProcessSelected(selected)
            }.onFailure {
                processOperationError = it
            }
        }
    }
    

    fun updateProcess(process: Process) {
        val trimmedName = process.name.trim()
        val trimmedUnit = process.unit.trim()
        if (!isValidProcessInput(trimmedName, process.defaultPrice, trimmedUnit)) {
            processOperationError = InvalidProcessInputException()
            return
        }

        viewModelScope.launch {
            runCatching {
                val existing = processRepository.getProcessByName(trimmedName)
                if (existing != null && existing.id != process.id) {
                    throw ProcessAlreadyExistsException(trimmedName)
                }
                val normalized = process.copy(
                    name = trimmedName,
                    unit = trimmedUnit,
                    isActive = true
                )
                processRepository.updateProcess(normalized)
                if (workRecordUiState.workRecordDetails.processId == normalized.id) {
                    updateUiState(workRecordUiState.workRecordDetails.copy(
                        processName = normalized.name,
                        unitPrice = normalized.defaultPrice.toString()
                    ))
                }
            }.onFailure {
                processOperationError = it
            }
        }
    }

    fun deleteProcess(process: Process) {
        viewModelScope.launch {
            runCatching {
                processRepository.deleteProcess(process)
                if (workRecordUiState.workRecordDetails.processId == process.id) {
                    updateUiState(workRecordUiState.workRecordDetails.copy(
                        processId = null,
                        processName = "",
                        unitPrice = ""
                    ))
                }
            }.onFailure {
                processOperationError = it
            }
        }
    }

    fun addNewStyle(styleName: String) {
        val normalized = styleName.trim()
        if (normalized.isBlank()) return
        viewModelScope.launch {
            val existing = styleRepository.getStyleByName(normalized)
            if (existing == null) {
                styleRepository.insertStyle(Style(name = normalized))
            }
            onStyleSelected(normalized)
        }
    }

    private fun validateInput(uiState: WorkRecordDetails = workRecordUiState.workRecordDetails): Boolean {
        return with(uiState) {
            val quantityValue = quantity.toLongOrNull()
            val unitPriceValue = normalizeDecimalInput(unitPrice).toDoubleOrNull()
            style.isNotBlank() && 
            processName.isNotBlank() && 
            quantityValue != null &&
            quantityValue > 0L &&
            unitPriceValue != null &&
            unitPriceValue >= 0.0 &&
            date > 0
        }
    }

    private fun isValidProcessInput(name: String, defaultPrice: Double, unit: String): Boolean {
        return name.isNotBlank() &&
            unit.isNotBlank() &&
            defaultPrice.isFinite() &&
            defaultPrice >= 0.0
    }

    private fun asColorOperationError(error: Throwable): Throwable {
        return when (error) {
            is ColorPresetNameConflictException ->
                ColorPresetAlreadyExistsException(error.presetName)
            is ColorGroupNameConflictException ->
                ColorGroupAlreadyExistsException(error.groupName)
            is InvalidColorPresetInputException,
            is ColorPresetAlreadyExistsException,
            is InvalidColorGroupInputException,
            is ColorGroupAlreadyExistsException,
            is ColorOperationFailedException -> error
            else -> ColorOperationFailedException()
        }
    }

    suspend fun saveWorkRecord(): Result<Unit> {
        if (!validateInput()) {
            return Result.failure(InvalidWorkRecordInputException())
        }
        return runCatching {
            val normalizedStyle = workRecordUiState.workRecordDetails.style.trim()
            val record = workRecordUiState.workRecordDetails
                .copy(style = normalizedStyle)
                .toWorkRecord()
            val images = workRecordUiState.workRecordDetails.imagePaths
            val colorItems = workRecordUiState.workRecordDetails.colorEntries.mapIndexed { index, entry ->
                com.example.processrecord.data.entity.WorkRecordColorItem(
                    workRecordId = 0, // Filled with actual id inside transaction
                    colorName = entry.colorName,
                    colorHex = normalizeHex(entry.colorHex),
                    quantity = entry.quantity.toLongOrNull() ?: 0L,
                    deficit = entry.deficit.toLongOrNull() ?: 0L,
                    colorCode = entry.colorCode,
                    sortOrder = index
                )
            }

            if (normalizedStyle.isNotBlank()) {
                val existingStyle = styleRepository.getStyleByName(normalizedStyle)
                if (existingStyle == null) {
                    styleRepository.insertStyle(Style(name = normalizedStyle))
                }
            }

            if (recordId != null) {
                workRecordRepository.updateRecordWithDetails(
                    record = record.copy(id = recordId),
                    images = images,
                    colorItems = colorItems
                )
            } else {
                workRecordRepository.insertRecordWithDetails(
                    record = record,
                    images = images,
                    colorItems = colorItems
                )
            }
        }
    }

    suspend fun deleteRecord(): Result<Unit> {
        val targetRecordId = recordId ?: return Result.failure(WorkRecordNotFoundException())
        return runCatching {
            workRecordRepository.deleteRecord(
                workRecordUiState.workRecordDetails.toWorkRecord().copy(id = targetRecordId)
            )
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
    val quantity: String,
    val deficit: String = "",  // Deficit
    val colorCode: String = ""  // Color code
)

private fun formatQuantity(value: Long): String {
    return value.toString()
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
        text.contains("red") || text.contains("\u7ea2") -> "#F44336"
        text.contains("orange") || text.contains("\u6a59") -> "#FF9800"
        text.contains("yellow") || text.contains("\u9ec4") -> "#FFEB3B"
        text.contains("army green") || text.contains("\u519b\u7eff") -> "#4B5320"
        text.contains("green") || text.contains("\u7eff") -> "#4CAF50"
        text.contains("cyan") || text.contains("teal") || text.contains("\u9752") -> "#00BCD4"
        text.contains("navy") || text.contains("dark blue") || text.contains("\u85cf\u84dd") || text.contains("\u6df1\u84dd") -> "#1F3A5F"
        text.contains("blue") || text.contains("\u84dd") -> "#2196F3"
        text.contains("purple") || text.contains("violet") || text.contains("\u7d2b") -> "#9C27B0"
        text.contains("white") || text.contains("beige") || text.contains("\u767d") || text.contains("\u7c73") -> "#F5F5DC"
        text.contains("khaki") || text.contains("\u5361\u5176") -> "#C3B091"
        text.contains("camel") || text.contains("\u9a7c") -> "#B8860B"
        text.contains("coffee") || text.contains("brown") || text.contains("\u5496\u5561") || text.contains("\u68d5") -> "#6F4E37"
        text.contains("burgundy") || text.contains("wine") || text.contains("\u9152\u7ea2") -> "#8B1A1A"
        text.contains("pink") || text.contains("\u7c89") -> "#F48FB1"
        text.contains("sky blue") || text.contains("\u5929\u84dd") -> "#87CEEB"
        text.contains("light gray") || text.contains("\u6d45\u7070") -> "#E0E0E0"
        text.contains("dark gray") || text.contains("\u6df1\u7070") -> "#616161"
        text.contains("gray") || text.contains("grey") || text.contains("\u7070") -> "#9E9E9E"
        text.contains("black") || text.contains("\u9ed1") -> "#212121"
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
        ColorEntryUi(name, suggestHexByName(name) ?: "#9E9E9E", qty)
    }.toList()
    return if (matched.isNotEmpty()) matched else listOf(ColorEntryUi(text.trim(), "#9E9E9E", ""))
}

