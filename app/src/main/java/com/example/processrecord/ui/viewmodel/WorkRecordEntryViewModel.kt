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
import com.example.processrecord.data.WorkRecordInsertPayload
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.data.entity.Process
import com.example.processrecord.data.entity.Style
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

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

enum class WorkRecordFieldError {
    Required,
    InvalidNumber,
    MustBePositive,
    MustBeNonNegative
}

data class WorkRecordValidationErrors(
    val style: WorkRecordFieldError? = null,
    val processName: WorkRecordFieldError? = null,
    val quantity: WorkRecordFieldError? = null,
    val unitPrice: WorkRecordFieldError? = null
)

data class WorkRecordLastUsedDefaults(
    val processId: Long? = null,
    val processName: String = "",
    val style: String = "",
    val unitPrice: String = ""
) {
    val hasContent: Boolean
        get() = processName.isNotBlank() || style.isNotBlank() || unitPrice.isNotBlank()
}

data class WorkRecordUiState(
    val workRecordDetails: WorkRecordDetails = WorkRecordDetails(),
    val isEntryValid: Boolean = false,
    val validationErrors: WorkRecordValidationErrors = WorkRecordValidationErrors(),
    val hasRequestedValidation: Boolean = false,
    val lastUsedDefaults: WorkRecordLastUsedDefaults? = null
)

data class WorkRecordGroupUiState(
    val style: String = "",
    val items: List<WorkRecordDetails> = listOf(WorkRecordDetails()),
    val isEntryValid: Boolean = false,
    val styleError: WorkRecordFieldError? = null,
    val itemValidationErrors: List<WorkRecordValidationErrors> = emptyList(),
    val hasRequestedValidation: Boolean = false
)

data class WorkRecordDetails(
    val id: Long = 0,
    val processId: Long? = null,
    val processName: String = "",
    val style: String = "",
    val entryGroupId: String = "",
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
    val date: Long = System.currentTimeMillis(),
    val createTime: Long = System.currentTimeMillis()
)

data class ColorEntryUi(
    val colorName: String,
    val colorHex: String,
    val quantity: String,
    val deficit: String = "",
    val isDeficitResolved: Boolean = false,
    val colorCode: String = ""
)

data class ExistingWorkRecordGroupUiState(
    val entryGroupId: String = "",
    val items: List<WorkRecordDetails> = emptyList(),
    val itemValidationErrors: List<WorkRecordValidationErrors> = emptyList(),
    val hasRequestedValidation: Boolean = false,
    val isLoading: Boolean = false
)

class WorkRecordEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val workRecordRepository: WorkRecordRepository,
    private val processRepository: ProcessRepository,
    private val styleRepository: StyleRepository
) : ViewModel() {

    private val recordId: Long? = savedStateHandle.get<String>("recordId")?.toLongOrNull()
    private val appendToGroupId: String? = savedStateHandle.get<String>("appendToGroupId")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    private val initialRecordId: Long? = savedStateHandle.get<String>("initialRecordId")?.toLongOrNull()

    private var loadedEntryGroupId: String? = null
    private var persistedExistingGroupItems: List<WorkRecordDetails> = emptyList()
    var initialExistingItemIndex by mutableStateOf<Int?>(null)
        private set

    var groupUiState by mutableStateOf(
        WorkRecordGroupUiState(
            items = listOf(emptyNewEntryDetails())
        )
    )
        private set

    var workRecordUiState by mutableStateOf(WorkRecordUiState())
        private set

    var existingGroupUiState by mutableStateOf(
        ExistingWorkRecordGroupUiState(
            entryGroupId = appendToGroupId.orEmpty(),
            isLoading = appendToGroupId != null
        )
    )
        private set

    var processOperationError by mutableStateOf<Throwable?>(null)
        private set
    var colorManageOperationError by mutableStateOf<Throwable?>(null)
        private set
    var colorManageOperationNotice by mutableStateOf<ColorManageOperationNotice?>(null)
        private set

    val groupSharedDetails: WorkRecordDetails
        get() = existingGroupUiState.items.firstOrNull()
            ?: groupUiState.items.firstOrNull()
            ?: WorkRecordDetails()

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

    val isSingleRecordEditMode: Boolean
        get() = recordId != null

    val isAppendToExistingGroupMode: Boolean
        get() = appendToGroupId != null && recordId == null

    init {
        when {
            recordId != null -> {
                viewModelScope.launch {
                    loadSingleRecord(sourceRecordId = recordId)
                }
            }

            appendToGroupId != null -> {
                viewModelScope.launch {
                    loadAppendTargetGroup(appendToGroupId)
                }
            }

            else -> syncSingleUiState()
        }
    }

    private suspend fun loadSingleRecord(sourceRecordId: Long) {
        val record = workRecordRepository.getRecordStream(sourceRecordId) ?: return
        val details = buildRecordDetails(record)
        loadedEntryGroupId = normalizeEntryGroupId(record)
        persistedExistingGroupItems = emptyList()
        initialExistingItemIndex = null
        setGroupUiState(
            style = details.style,
            items = listOf(details),
            hasRequestedValidation = false,
            lastUsedDefaults = details.toLastUsedDefaults()
        )
    }

    private suspend fun loadAppendTargetGroup(entryGroupId: String) {
        val records = workRecordRepository.getRecordsByGroupId(entryGroupId)
            .sortedWith(compareBy<WorkRecord> { it.createTime }.thenBy { it.id })
        loadedEntryGroupId = entryGroupId
        val style = records.firstOrNull()?.style.orEmpty()
        val rawExistingItems = records.map { record ->
            buildRecordDetails(record).copy(style = style)
        }
        val sharedFields = resolveSharedGroupFields(existingItems = rawExistingItems)
        val existingItems = applySharedGroupFields(rawExistingItems, sharedFields)
        persistedExistingGroupItems = existingItems
        initialExistingItemIndex = initialRecordId
            ?.let { targetRecordId ->
                existingItems.indexOfFirst { it.id == targetRecordId }
                    .takeIf { it >= 0 }
            }
        setGroupUiState(
            style = style,
            items = listOf(
                applySharedGroupFields(
                    emptyNewEntryDetails(style = style, date = sharedFields.date),
                    sharedFields
                )
            ),
            hasRequestedValidation = false,
            lastUsedDefaults = null
        )
        setExistingGroupUiState(
            items = existingItems,
            entryGroupId = entryGroupId,
            hasRequestedValidation = false,
            isLoading = false
        )
    }

    private suspend fun buildRecordDetails(record: WorkRecord): WorkRecordDetails {
        val images = workRecordRepository.getImagesForRecord(record.id)
        val colorItems = workRecordRepository.getColorItemsForRecord(record.id)
        val colorEntries = if (colorItems.isNotEmpty()) {
            colorItems.map {
                ColorEntryUi(
                    colorName = it.colorName,
                    colorHex = it.colorHex,
                    quantity = formatQuantity(it.quantity),
                    deficit = it.deficit,
                    isDeficitResolved = it.isDeficitResolved,
                    colorCode = it.colorCode
                ).normalizeDeficitState()
            }
        } else {
            parseLegacyColorEntries(record.color)
        }

        return record.toWorkRecordDetails().copy(
            colorEntries = colorEntries,
            imagePaths = images
        )
    }

    fun updateGroupStyle(style: String) {
        setGroupUiState(style = style, items = groupUiState.items)
        if (isAppendToExistingGroupMode) {
            setExistingGroupUiState(
                items = existingGroupUiState.items,
                entryGroupId = loadedEntryGroupId ?: existingGroupUiState.entryGroupId
            )
        }
    }

    fun updateUiState(recordDetails: WorkRecordDetails) {
        updateProcessItem(0, recordDetails)
    }

    fun updateProcessItem(index: Int, recordDetails: WorkRecordDetails) {
        if (index !in groupUiState.items.indices) return
        val updatedStyle = if (index == 0 && recordDetails.style != groupUiState.style) {
            recordDetails.style
        } else {
            groupUiState.style
        }
        val updatedItems = groupUiState.items.toMutableList()
        updatedItems[index] = recordDetails.copy(style = updatedStyle)
        setGroupUiState(style = updatedStyle, items = updatedItems)
    }

    fun updateExistingProcessItem(index: Int, recordDetails: WorkRecordDetails) {
        if (index !in existingGroupUiState.items.indices) return
        val updatedItems = existingGroupUiState.items.toMutableList()
        updatedItems[index] = recordDetails.copy(style = groupUiState.style)
        setExistingGroupUiState(items = updatedItems)
    }

    fun addProcessItem() {
        if (isSingleRecordEditMode) return
        val sharedFields = currentSharedGroupFields()
        setGroupUiState(
            style = groupUiState.style,
            items = groupUiState.items + applySharedGroupFields(
                emptyNewEntryDetails(
                    style = groupUiState.style,
                    date = sharedFields.date
                ),
                sharedFields
            )
        )
    }

    fun removeProcessItem(index: Int) {
        if (index !in groupUiState.items.indices) return
        if (groupUiState.items.size <= 1) return
        val updatedItems = groupUiState.items.toMutableList().also { it.removeAt(index) }
        setGroupUiState(style = groupUiState.style, items = updatedItems)
    }

    fun onProcessSelected(process: Process) {
        onProcessSelected(0, process)
    }

    fun onProcessSelected(index: Int, process: Process) {
        val currentDetails = groupUiState.items.getOrNull(index) ?: return
        val newDetails = currentDetails.copy(
            processId = process.id,
            processName = process.name,
            unitPrice = process.defaultPrice.toString()
        )
        updateProcessItem(index, newDetails)
    }

    fun onExistingProcessSelected(index: Int, process: Process) {
        val currentDetails = existingGroupUiState.items.getOrNull(index) ?: return
        val newDetails = currentDetails.copy(
            processId = process.id,
            processName = process.name,
            unitPrice = process.defaultPrice.toString()
        )
        updateExistingProcessItem(index, newDetails)
    }

    fun onStyleSelected(styleName: String) {
        updateGroupStyle(styleName)
    }

    fun updateSharedDate(date: Long) {
        if (date <= 0L) return
        setGroupUiState(
            style = groupUiState.style,
            items = groupUiState.items.map { it.copy(date = date) }
        )
    }

    fun updateSharedTotalQuantity(totalQuantity: String) {
        val filtered = totalQuantity.filter(Char::isDigit)
        if (filtered.isNotEmpty() && filtered.toLongOrNull() == null) return
        applySharedFieldsToGroupState(currentSharedGroupFields().copy(totalQuantity = filtered))
    }

    fun updateSharedImagePaths(imagePaths: List<String>) {
        applySharedFieldsToGroupState(currentSharedGroupFields().copy(imagePaths = imagePaths))
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
        addColorEntryFromPreset(0, colorName, colorHex)
    }

    fun addColorEntryFromPreset(index: Int, colorName: String, colorHex: String) {
        val current = groupUiState.items.getOrNull(index) ?: return
        if (current.colorEntries.any { it.colorName == colorName }) return
        onColorEntriesChanged(
            index = index,
            entries = current.colorEntries + ColorEntryUi(
                colorName = colorName,
                colorHex = colorHex,
                quantity = ""
            )
        )
    }

    fun addColorEntryFromExistingPreset(index: Int, colorName: String, colorHex: String) {
        val current = existingGroupUiState.items.getOrNull(index) ?: return
        if (current.colorEntries.any { it.colorName == colorName }) return
        onExistingColorEntriesChanged(
            index = index,
            entries = current.colorEntries + ColorEntryUi(
                colorName = colorName,
                colorHex = colorHex,
                quantity = ""
            )
        )
    }

    fun updateColorEntryQuantity(colorName: String, quantity: String) {
        updateColorEntryQuantity(0, colorName, quantity)
    }

    fun updateColorEntryQuantity(index: Int, colorName: String, quantity: String) {
        updateColorEntry(index, colorName) { it.copy(quantity = quantity) }
    }

    fun updateExistingColorEntryQuantity(index: Int, colorName: String, quantity: String) {
        updateExistingColorEntry(index, colorName) { it.copy(quantity = quantity) }
    }

    fun updateColorEntryDeficit(colorName: String, deficit: String) {
        updateColorEntryDeficit(0, colorName, deficit)
    }

    fun updateColorEntryDeficit(index: Int, colorName: String, deficit: String) {
        updateColorEntry(index, colorName) {
            it.copy(
                deficit = deficit,
                isDeficitResolved = it.isDeficitResolved && deficit.trim().isNotBlank()
            )
        }
    }

    fun updateExistingColorEntryDeficit(index: Int, colorName: String, deficit: String) {
        updateExistingColorEntry(index, colorName) {
            it.copy(
                deficit = deficit,
                isDeficitResolved = it.isDeficitResolved && deficit.trim().isNotBlank()
            )
        }
    }

    fun toggleColorEntryDeficitResolved(colorName: String) {
        toggleColorEntryDeficitResolved(0, colorName)
    }

    fun toggleColorEntryDeficitResolved(index: Int, colorName: String) {
        updateColorEntry(index, colorName) { entry ->
            if (entry.deficit.trim().isBlank()) {
                entry.copy(isDeficitResolved = false)
            } else {
                entry.copy(isDeficitResolved = !entry.isDeficitResolved)
            }
        }
    }

    fun toggleExistingColorEntryDeficitResolved(index: Int, colorName: String) {
        updateExistingColorEntry(index, colorName) { entry ->
            if (entry.deficit.trim().isBlank()) {
                entry.copy(isDeficitResolved = false)
            } else {
                entry.copy(isDeficitResolved = !entry.isDeficitResolved)
            }
        }
    }

    fun updateColorEntryColorCode(colorName: String, colorCode: String) {
        updateColorEntryColorCode(0, colorName, colorCode)
    }

    fun updateColorEntryColorCode(index: Int, colorName: String, colorCode: String) {
        updateColorEntry(index, colorName) { it.copy(colorCode = colorCode) }
    }

    fun updateExistingColorEntryColorCode(index: Int, colorName: String, colorCode: String) {
        updateExistingColorEntry(index, colorName) { it.copy(colorCode = colorCode) }
    }

    fun removeColorEntry(colorName: String) {
        removeColorEntry(0, colorName)
    }

    fun removeColorEntry(index: Int, colorName: String) {
        val current = groupUiState.items.getOrNull(index) ?: return
        onColorEntriesChanged(
            index = index,
            entries = current.colorEntries.filterNot { it.colorName == colorName }
        )
    }

    fun removeExistingColorEntry(index: Int, colorName: String) {
        val current = existingGroupUiState.items.getOrNull(index) ?: return
        onExistingColorEntriesChanged(
            index = index,
            entries = current.colorEntries.filterNot { it.colorName == colorName }
        )
    }

    private fun updateColorEntry(
        index: Int,
        colorName: String,
        transform: (ColorEntryUi) -> ColorEntryUi
    ) {
        val current = groupUiState.items.getOrNull(index) ?: return
        val updated = current.colorEntries.map {
            if (it.colorName == colorName) transform(it) else it
        }
        onColorEntriesChanged(index, updated)
    }

    private fun updateExistingColorEntry(
        index: Int,
        colorName: String,
        transform: (ColorEntryUi) -> ColorEntryUi
    ) {
        val current = existingGroupUiState.items.getOrNull(index) ?: return
        val updated = current.colorEntries.map {
            if (it.colorName == colorName) transform(it) else it
        }
        onExistingColorEntriesChanged(index, updated)
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
        onColorEntriesChanged(0, entries)
    }

    fun onColorEntriesChanged(index: Int, entries: List<ColorEntryUi>) {
        val normalizedEntries = entries.map(ColorEntryUi::normalizeDeficitState)
        val current = groupUiState.items.getOrNull(index) ?: return
        val hasAnyQuantity = normalizedEntries.any {
            it.quantity.toLongOrNull() != null && it.quantity.isNotBlank()
        }
        val colorSummary = buildColorSummary(normalizedEntries)
        val newDetails = if (hasAnyQuantity) {
            val totalQuantity = normalizedEntries.sumOf { it.quantity.toLongOrNull() ?: 0L }
            current.copy(
                colorEntries = normalizedEntries,
                color = colorSummary,
                quantity = totalQuantity.toString()
            )
        } else {
            current.copy(
                colorEntries = normalizedEntries,
                color = colorSummary
            )
        }
        updateProcessItem(index, newDetails)
    }

    fun onExistingColorEntriesChanged(index: Int, entries: List<ColorEntryUi>) {
        val normalizedEntries = entries.map(ColorEntryUi::normalizeDeficitState)
        val current = existingGroupUiState.items.getOrNull(index) ?: return
        val hasAnyQuantity = normalizedEntries.any {
            it.quantity.toLongOrNull() != null && it.quantity.isNotBlank()
        }
        val colorSummary = buildColorSummary(normalizedEntries)
        val newDetails = if (hasAnyQuantity) {
            val totalQuantity = normalizedEntries.sumOf { it.quantity.toLongOrNull() ?: 0L }
            current.copy(
                colorEntries = normalizedEntries,
                color = colorSummary,
                quantity = totalQuantity.toString()
            )
        } else {
            current.copy(
                colorEntries = normalizedEntries,
                color = colorSummary
            )
        }
        updateExistingProcessItem(index, newDetails)
    }

    fun deleteStyle(styleName: String) {
        viewModelScope.launch {
            styleRepository.deleteStyleByName(styleName)
        }
    }

    fun addProcess(name: String, defaultPrice: Double, unit: String) {
        addProcess(0, name, defaultPrice, unit)
    }

    fun addProcess(index: Int, name: String, defaultPrice: Double, unit: String) {
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
                onProcessSelected(index, selected)
            }.onFailure {
                processOperationError = it
            }
        }
    }

    fun addProcessToExisting(index: Int, name: String, defaultPrice: Double, unit: String) {
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
                onExistingProcessSelected(index, selected)
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
                val updatedItems = groupUiState.items.map { item ->
                    if (item.processId == normalized.id) {
                        item.copy(
                            processName = normalized.name,
                            unitPrice = normalized.defaultPrice.toString()
                        )
                    } else {
                        item
                    }
                }
                val updatedExistingItems = existingGroupUiState.items.map { item ->
                    if (item.processId == normalized.id) {
                        item.copy(
                            processName = normalized.name,
                            unitPrice = normalized.defaultPrice.toString()
                        )
                    } else {
                        item
                    }
                }
                setGroupUiState(style = groupUiState.style, items = updatedItems)
                setExistingGroupUiState(items = updatedExistingItems)
            }.onFailure {
                processOperationError = it
            }
        }
    }

    fun deleteProcess(process: Process) {
        viewModelScope.launch {
            runCatching {
                processRepository.deleteProcess(process)
                val updatedItems = groupUiState.items.map { item ->
                    if (item.processId == process.id) {
                        item.copy(
                            processId = null,
                            processName = "",
                            unitPrice = ""
                        )
                    } else {
                        item
                    }
                }
                val updatedExistingItems = existingGroupUiState.items.map { item ->
                    if (item.processId == process.id) {
                        item.copy(
                            processId = null,
                            processName = "",
                            unitPrice = ""
                        )
                    } else {
                        item
                    }
                }
                setGroupUiState(style = groupUiState.style, items = updatedItems)
                setExistingGroupUiState(items = updatedExistingItems)
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

    suspend fun saveWorkRecord(): Result<Unit> {
        setGroupUiState(
            style = groupUiState.style,
            items = groupUiState.items,
            hasRequestedValidation = true
        )
        val normalizedStyle = groupUiState.style.trim()
        val sharedFields = currentSharedGroupFields()
        val normalizedItems = applySharedGroupFields(groupUiState.items, sharedFields).map {
            recomputeAmount(it.copy(style = normalizedStyle))
        }
        if (!validateGroup(normalizedStyle, normalizedItems)) {
            return Result.failure(InvalidWorkRecordInputException())
        }

        return runCatching {
            ensureStyleExists(normalizedStyle)

            if (recordId != null) {
                val item = normalizedItems.first()
                val entryGroupId = loadedEntryGroupId ?: "legacy_$recordId"
                workRecordRepository.updateRecordWithDetails(
                    record = item.toWorkRecord().copy(
                        id = recordId,
                        entryGroupId = entryGroupId
                    ),
                    images = item.imagePaths,
                    colorItems = buildColorItems(item)
                )
                item.toLastUsedDefaults()
            } else {
                val groupId = loadedEntryGroupId ?: UUID.randomUUID().toString()
                val baseCreateTime = System.currentTimeMillis()
                val payloads = normalizedItems.mapIndexed { index, item ->
                    WorkRecordInsertPayload(
                        record = item.copy(
                            id = 0,
                            style = normalizedStyle,
                            createTime = baseCreateTime + index
                        ).toWorkRecord().copy(entryGroupId = groupId),
                        images = item.imagePaths,
                        colorItems = buildColorItems(item)
                    )
                }
                syncPersistedExistingGroupSharedFields(
                    style = normalizedStyle,
                    sharedFields = sharedFields
                )
                workRecordRepository.insertRecordGroupWithDetails(payloads)
                normalizedItems.first().toLastUsedDefaults().copy(style = normalizedStyle)
            }
        }.map { savedDefaults ->
            if (recordId == null) {
                val resetDate = normalizedItems.firstOrNull()?.date ?: System.currentTimeMillis()
                setGroupUiState(
                    style = "",
                    items = listOf(emptyNewEntryDetails(date = resetDate)),
                    hasRequestedValidation = false,
                    lastUsedDefaults = savedDefaults
                )
            } else {
                setGroupUiState(
                    style = normalizedStyle,
                    items = normalizedItems,
                    hasRequestedValidation = false,
                    lastUsedDefaults = savedDefaults
                )
            }
        }
    }

    suspend fun saveExistingRecord(index: Int): Result<Unit> {
        if (index !in existingGroupUiState.items.indices) {
            return Result.failure(WorkRecordNotFoundException())
        }
        val sharedFields = currentSharedGroupFields()
        val synchronizedExistingItems = applySharedGroupFields(existingGroupUiState.items, sharedFields)
        setExistingGroupUiState(
            items = synchronizedExistingItems,
            entryGroupId = loadedEntryGroupId ?: existingGroupUiState.entryGroupId,
            hasRequestedValidation = true
        )

        val normalizedStyle = groupUiState.style.trim()
        val currentItem = synchronizedExistingItems.getOrNull(index)
            ?.copy(style = normalizedStyle)
            ?.let(::recomputeAmount)
            ?: return Result.failure(WorkRecordNotFoundException())

        if (!validateInput(currentItem)) {
            return Result.failure(InvalidWorkRecordInputException())
        }

        return runCatching {
            ensureStyleExists(normalizedStyle)
            val entryGroupId = loadedEntryGroupId ?: "legacy_${currentItem.id}"
            workRecordRepository.updateRecordWithDetails(
                record = currentItem.toWorkRecord().copy(
                    id = currentItem.id,
                    entryGroupId = entryGroupId
                ),
                images = currentItem.imagePaths,
                colorItems = buildColorItems(currentItem)
            )
            syncPersistedExistingGroupSharedFields(
                style = normalizedStyle,
                sharedFields = sharedFields,
                skipRecordId = currentItem.id
            )
        }.map {
            val updatedItems = synchronizedExistingItems.toMutableList()
            updatedItems[index] = currentItem
            persistedExistingGroupItems = persistedExistingGroupItems
                .map { applySharedGroupFields(it, sharedFields).copy(style = normalizedStyle) }
                .map { item -> if (item.id == currentItem.id) currentItem else item }
            setExistingGroupUiState(
                items = updatedItems,
                entryGroupId = loadedEntryGroupId ?: existingGroupUiState.entryGroupId,
                hasRequestedValidation = false
            )
        }
    }

    suspend fun deleteRecord(): Result<Unit> {
        val targetRecordId = recordId ?: return Result.failure(WorkRecordNotFoundException())
        val current = groupUiState.items.firstOrNull() ?: return Result.failure(WorkRecordNotFoundException())
        return runCatching {
            val entryGroupId = loadedEntryGroupId ?: "legacy_$targetRecordId"
            workRecordRepository.deleteRecord(
                current.copy(style = groupUiState.style).toWorkRecord().copy(
                    id = targetRecordId,
                    entryGroupId = entryGroupId
                )
            )
        }
    }

    fun deleteExistingGroupRecord(index: Int) {
        if (index !in existingGroupUiState.items.indices) return
        val currentItems = existingGroupUiState.items
        val target = currentItems[index]
        viewModelScope.launch {
            workRecordRepository.deleteRecord(
                target.copy(style = groupUiState.style).toWorkRecord().copy(
                    id = target.id,
                    entryGroupId = loadedEntryGroupId ?: "legacy_${target.id}"
                )
            )
            val updatedItems = currentItems.toMutableList().also { it.removeAt(index) }
            persistedExistingGroupItems = persistedExistingGroupItems.filterNot { it.id == target.id }
            setExistingGroupUiState(
                items = updatedItems,
                entryGroupId = loadedEntryGroupId ?: existingGroupUiState.entryGroupId,
                hasRequestedValidation = false
            )
        }
    }

    private suspend fun syncPersistedExistingGroupSharedFields(
        style: String,
        sharedFields: WorkRecordDetails,
        skipRecordId: Long? = null
    ) {
        if (persistedExistingGroupItems.isEmpty()) return
        val normalizedEntryGroupId = loadedEntryGroupId ?: existingGroupUiState.entryGroupId
        persistedExistingGroupItems
            .filterNot { it.id == skipRecordId }
            .map { applySharedGroupFields(it, sharedFields).copy(style = style) }
            .forEach { item ->
                val entryGroupId = normalizedEntryGroupId.ifBlank { "legacy_${item.id}" }
                workRecordRepository.updateRecordWithDetails(
                    record = item.toWorkRecord().copy(
                        id = item.id,
                        entryGroupId = entryGroupId
                    ),
                    images = item.imagePaths,
                    colorItems = buildColorItems(item)
                )
            }
        persistedExistingGroupItems = persistedExistingGroupItems
            .map { applySharedGroupFields(it, sharedFields).copy(style = style) }
    }

    private suspend fun ensureStyleExists(styleName: String) {
        if (styleName.isBlank()) return
        val existingStyle = styleRepository.getStyleByName(styleName)
        if (existingStyle == null) {
            styleRepository.insertStyle(Style(name = styleName))
        }
    }

    private fun buildColorItems(item: WorkRecordDetails): List<WorkRecordColorItem> {
        return item.colorEntries.mapIndexed { index, entry ->
            val normalizedDeficit = entry.deficit.trim()
            WorkRecordColorItem(
                workRecordId = 0,
                colorName = entry.colorName,
                colorHex = normalizeHex(entry.colorHex),
                quantity = entry.quantity.toLongOrNull() ?: 0L,
                deficit = normalizedDeficit,
                isDeficitResolved = normalizedDeficit.isNotBlank() && entry.isDeficitResolved,
                colorCode = entry.colorCode,
                sortOrder = index
            )
        }
    }

    private fun validateGroup(style: String, items: List<WorkRecordDetails>): Boolean {
        return style.isNotBlank() &&
            items.isNotEmpty() &&
            items.all { validateInput(it.copy(style = style)) }
    }

    private fun currentSharedGroupFields(): WorkRecordDetails {
        return existingGroupUiState.items.firstOrNull()
            ?: groupUiState.items.firstOrNull()
            ?: emptyNewEntryDetails(style = groupUiState.style)
    }

    private fun resolveSharedGroupFields(
        existingItems: List<WorkRecordDetails> = existingGroupUiState.items,
        draftItems: List<WorkRecordDetails> = groupUiState.items
    ): WorkRecordDetails {
        return existingItems.firstOrNull()
            ?: draftItems.firstOrNull()
            ?: emptyNewEntryDetails(style = groupUiState.style)
    }

    private fun applySharedGroupFields(
        item: WorkRecordDetails,
        sharedFields: WorkRecordDetails
    ): WorkRecordDetails {
        return item.copy(
            totalQuantity = sharedFields.totalQuantity,
            imagePaths = sharedFields.imagePaths
        )
    }

    private fun applySharedGroupFields(
        items: List<WorkRecordDetails>,
        sharedFields: WorkRecordDetails
    ): List<WorkRecordDetails> {
        return items.map { applySharedGroupFields(it, sharedFields) }
    }

    private fun applySharedFieldsToGroupState(sharedFields: WorkRecordDetails) {
        setGroupUiState(
            style = groupUiState.style,
            items = applySharedGroupFields(groupUiState.items, sharedFields)
        )
        if (isAppendToExistingGroupMode) {
            setExistingGroupUiState(
                items = applySharedGroupFields(existingGroupUiState.items, sharedFields),
                entryGroupId = loadedEntryGroupId ?: existingGroupUiState.entryGroupId
            )
        }
    }

    private fun setGroupUiState(
        style: String,
        items: List<WorkRecordDetails>,
        hasRequestedValidation: Boolean = groupUiState.hasRequestedValidation,
        lastUsedDefaults: WorkRecordLastUsedDefaults? = workRecordUiState.lastUsedDefaults
    ) {
        val normalizedStyle = style
        val normalizedItems = items.ifEmpty {
            listOf(emptyNewEntryDetails(style = normalizedStyle))
        }.map {
            recomputeAmount(it.copy(style = normalizedStyle))
        }

        val itemErrors = if (hasRequestedValidation) {
            normalizedItems.map(::buildValidationErrors)
        } else {
            List(normalizedItems.size) { WorkRecordValidationErrors() }
        }
        val styleError = itemErrors.firstOrNull()?.style
        val isEntryValid = validateGroup(normalizedStyle, normalizedItems)

        groupUiState = WorkRecordGroupUiState(
            style = normalizedStyle,
            items = normalizedItems,
            isEntryValid = isEntryValid,
            styleError = styleError,
            itemValidationErrors = itemErrors,
            hasRequestedValidation = hasRequestedValidation
        )
        syncSingleUiState(lastUsedDefaults)
    }

    private fun setExistingGroupUiState(
        items: List<WorkRecordDetails>,
        entryGroupId: String = existingGroupUiState.entryGroupId,
        hasRequestedValidation: Boolean = existingGroupUiState.hasRequestedValidation,
        isLoading: Boolean = existingGroupUiState.isLoading
    ) {
        val normalizedStyle = groupUiState.style
        val normalizedItems = items.map {
            recomputeAmount(it.copy(style = normalizedStyle))
        }
        val itemErrors = if (hasRequestedValidation) {
            normalizedItems.map(::buildValidationErrors)
        } else {
            List(normalizedItems.size) { WorkRecordValidationErrors() }
        }
        existingGroupUiState = ExistingWorkRecordGroupUiState(
            entryGroupId = entryGroupId,
            items = normalizedItems,
            itemValidationErrors = itemErrors,
            hasRequestedValidation = hasRequestedValidation,
            isLoading = isLoading
        )
    }

    private fun syncSingleUiState(
        lastUsedDefaults: WorkRecordLastUsedDefaults? = workRecordUiState.lastUsedDefaults
    ) {
        val firstItem = groupUiState.items.firstOrNull() ?: emptyNewEntryDetails(style = groupUiState.style)
        val firstErrors = groupUiState.itemValidationErrors.firstOrNull() ?: WorkRecordValidationErrors()
        workRecordUiState = WorkRecordUiState(
            workRecordDetails = firstItem.copy(style = groupUiState.style),
            isEntryValid = groupUiState.isEntryValid,
            validationErrors = firstErrors.copy(style = groupUiState.styleError),
            hasRequestedValidation = groupUiState.hasRequestedValidation,
            lastUsedDefaults = lastUsedDefaults
        )
    }

    private fun validateInput(uiState: WorkRecordDetails): Boolean {
        return with(uiState) {
            val quantityValue = quantity.toLongOrNull()
            val unitPriceValue = normalizeDecimalInput(unitPrice).toDoubleOrNull()
            style.trim().isNotBlank() &&
                processName.trim().isNotBlank() &&
                quantityValue != null &&
                quantityValue > 0L &&
                unitPriceValue != null &&
                unitPriceValue >= 0.0 &&
                date > 0
        }
    }

    private fun buildValidationErrors(uiState: WorkRecordDetails): WorkRecordValidationErrors {
        val quantityValue = uiState.quantity.toLongOrNull()
        val unitPriceText = normalizeDecimalInput(uiState.unitPrice)
        val unitPriceValue = unitPriceText.toDoubleOrNull()

        return WorkRecordValidationErrors(
            style = if (uiState.style.trim().isBlank()) WorkRecordFieldError.Required else null,
            processName = if (uiState.processName.trim().isBlank()) {
                WorkRecordFieldError.Required
            } else {
                null
            },
            quantity = when {
                uiState.quantity.trim().isBlank() -> WorkRecordFieldError.Required
                quantityValue == null -> WorkRecordFieldError.InvalidNumber
                quantityValue <= 0L -> WorkRecordFieldError.MustBePositive
                else -> null
            },
            unitPrice = when {
                unitPriceText.isBlank() -> WorkRecordFieldError.Required
                unitPriceValue == null -> WorkRecordFieldError.InvalidNumber
                unitPriceValue < 0.0 -> WorkRecordFieldError.MustBeNonNegative
                else -> null
            }
        )
    }

    private fun recomputeAmount(details: WorkRecordDetails): WorkRecordDetails {
        val quantity = details.quantity.toLongOrNull() ?: 0L
        val unitPriceCents = yuanToCents(details.unitPrice)
        val amountCents = quantity * unitPriceCents
        val amountYuan = amountCents / 100.0
        return details.copy(
            amount = String.format(Locale.getDefault(), "%.2f", amountYuan)
        )
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

    private fun normalizeEntryGroupId(record: WorkRecord): String {
        return record.entryGroupId.ifBlank { "legacy_${record.id}" }
    }
}

private fun WorkRecordDetails.toLastUsedDefaults(): WorkRecordLastUsedDefaults {
    return WorkRecordLastUsedDefaults(
        processId = processId,
        processName = processName,
        style = style.trim(),
        unitPrice = normalizeDecimalInput(unitPrice)
    )
}

private fun emptyNewEntryDetails(
    style: String = "",
    date: Long = System.currentTimeMillis()
): WorkRecordDetails {
    val normalizedDate = if (date > 0L) date else System.currentTimeMillis()
    val now = System.currentTimeMillis()
    return WorkRecordDetails(
        style = style,
        date = normalizedDate,
        createTime = now
    )
}

private fun ColorEntryUi.normalizeDeficitState(): ColorEntryUi {
    return if (deficit.trim().isBlank() && isDeficitResolved) {
        copy(isDeficitResolved = false)
    } else {
        this
    }
}

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
