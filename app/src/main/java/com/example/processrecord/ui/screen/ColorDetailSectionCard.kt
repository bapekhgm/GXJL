package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.component.AppActionChip
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSelectableRow
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.viewmodel.ColorEntryUi
import com.example.processrecord.ui.viewmodel.WorkRecordDetails

private fun readableContentColor(background: Color): Color {
    val luminance = 0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    return if (luminance > 0.55f) {
        Color(0xFF102A43)
    } else {
        Color.White
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDetailSectionCard(
    workRecordDetails: WorkRecordDetails,
    colorGroups: List<ColorGroup>,
    colorPresets: List<ColorPreset>,
    onAddColorEntryFromPreset: (String, String) -> Unit,
    onUpdateColorEntryQuantity: (String, String) -> Unit,
    onUpdateColorEntryDeficit: (String, String) -> Unit,
    onUpdateColorEntryColorCode: (String, String) -> Unit,
    onRemoveColorEntry: (String) -> Unit,
    onManageColorPresetsClick: () -> Unit
) {
    var showAddColorSheet by remember { mutableStateOf(false) }
    val sheetGroupCollapsed = remember { mutableStateMapOf<Long, Boolean>() }
    val selectedColors = remember { mutableStateListOf<ColorPreset>() }

    SectionCard {
        SectionHeader(title = stringResource(R.string.work_record_section_color_detail)) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        brush = Brush.sweepGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AppPrimaryButton(
                text = stringResource(R.string.work_record_button_add_color),
                onClick = { showAddColorSheet = true },
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add,
                height = 48.dp
            )
            AppSecondaryButton(
                text = stringResource(R.string.work_record_button_manage_color_library),
                onClick = onManageColorPresetsClick,
                modifier = Modifier.weight(1f),
                height = 48.dp
            )
        }

        if (showAddColorSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddColorSheet = false
                    selectedColors.clear()
                },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val groupedFilteredPresets = colorGroups
                    .sortedBy { it.sortOrder }
                    .mapNotNull { group ->
                        val presets = colorPresets
                            .filter { it.groupId == group.id }
                            .sortedBy { it.sortOrder }
                        if (presets.isEmpty()) null else group to presets
                    }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 标题
                    item {
                        Text(
                            text = stringResource(R.string.work_record_sheet_select_color_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // 已选颜色标签
                    if (selectedColors.isNotEmpty()) {
                        item {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.work_record_sheet_selected_colors_count,
                                    selectedColors.size,
                                    selectedColors.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedColors.forEach { preset ->
                                    val chipColor = parseColorOrDefault(preset.hexValue)
                                    val chipContentColor = readableContentColor(chipColor)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                chipColor,
                                                RoundedCornerShape(50)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = preset.name,
                                                color = chipContentColor,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(
                                                    R.string.work_record_color_remove_content_description
                                                ),
                                                tint = chipContentColor,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { selectedColors.remove(preset) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 颜色分组列表
                    if (groupedFilteredPresets.isNotEmpty()) {
                        groupedFilteredPresets.forEach { (group, presets) ->
                            val collapsed = sheetGroupCollapsed[group.id] ?: false
                            item(key = "group_${group.id}") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    AppActionChip(
                                        text = stringResource(
                                            if (collapsed) {
                                                R.string.work_record_sheet_expand
                                            } else {
                                                R.string.work_record_sheet_collapse
                                            }
                                        ),
                                        onClick = { sheetGroupCollapsed[group.id] = !collapsed }
                                    )
                                }
                            }

                            if (!collapsed) {
                                presets.forEach { preset ->
                                    item(key = "preset_${preset.id}") {
                                        val isSelected = selectedColors.any { it.id == preset.id }
                                        AppSelectableRow(
                                            title = preset.name,
                                            subtitle = preset.hexValue,
                                            onClick = if (isSelected) null else {
                                                { selectedColors.add(preset) }
                                            },
                                            selected = isSelected,
                                            leadingContent = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(
                                                            color = parseColorOrDefault(preset.hexValue),
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                )
                                            },
                                            trailingContent = if (isSelected) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = stringResource(
                                                            R.string.work_record_color_selected_content_description
                                                        ),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.work_record_sheet_no_matching_colors),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 底部按钮
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AppPrimaryButton(
                                text = stringResource(R.string.work_record_button_done),
                                onClick = {
                                    selectedColors.forEach { preset ->
                                        onAddColorEntryFromPreset(preset.name, preset.hexValue)
                                    }
                                    showAddColorSheet = false
                                    selectedColors.clear()
                                },
                                enabled = selectedColors.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                height = 48.dp
                            )
                            AppSecondaryButton(
                                text = stringResource(R.string.common_cancel),
                                onClick = {
                                    showAddColorSheet = false
                                    selectedColors.clear()
                                },
                                modifier = Modifier.weight(1f),
                                height = 48.dp
                            )
                        }
                    }

                    item {
                        AppActionChip(
                            text = stringResource(R.string.work_record_button_go_manage_common_colors),
                            onClick = {
                                showAddColorSheet = false
                                selectedColors.clear()
                                onManageColorPresetsClick()
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                            emphasized = true
                        )
                    }
                }
            }
        }

        if (workRecordDetails.colorEntries.isNotEmpty()) {
            ColorQuantityTableHeader()
            workRecordDetails.colorEntries.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
                ColorQuantityTableRow(
                    entry = entry,
                    onQuantityChange = { qty -> onUpdateColorEntryQuantity(entry.colorName, qty) },
                    onDeficitChange = { deficit -> onUpdateColorEntryDeficit(entry.colorName, deficit) },
                    onColorCodeChange = { code -> onUpdateColorEntryColorCode(entry.colorName, code) },
                    onRemove = { onRemoveColorEntry(entry.colorName) }
                )
            }
        } else {
            Text(
                text = stringResource(R.string.work_record_color_detail_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ColorQuantityTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.color_table_header_color),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = stringResource(R.string.color_table_header_quantity),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = stringResource(R.string.color_table_header_deficit),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.7f)
        )
        Text(
            text = stringResource(R.string.color_table_header_color_code),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.8f)
        )
        Spacer(modifier = Modifier.width(36.dp))
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun ColorQuantityTableRow(
    entry: ColorEntryUi,
    onQuantityChange: (String) -> Unit,
    onDeficitChange: (String) -> Unit,
    onColorCodeChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 颜色名列
        Row(
            modifier = Modifier.weight(1.2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(parseColorOrDefault(entry.colorHex), shape = CircleShape)
            )
            Text(
                text = entry.colorName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        // 数量列
        UnderlineTextField(
            value = entry.quantity,
            onValueChange = { input ->
                val filtered = input.filter(Char::isDigit)
                if (filtered.isEmpty() || filtered.toLongOrNull() != null) {
                    onQuantityChange(filtered)
                }
            },
            placeholder = "0",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            modifier = Modifier.weight(0.8f)
        )
        // 欠数列
        UnderlineTextField(
            value = entry.deficit,
            onValueChange = { input ->
                val filtered = input.filter(Char::isDigit)
                if (filtered.isEmpty() || filtered.toLongOrNull() != null) {
                    onDeficitChange(filtered)
                }
            },
            placeholder = "0",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            modifier = Modifier.weight(0.7f)
        )
        // 色号列
        UnderlineTextField(
            value = entry.colorCode,
            onValueChange = onColorCodeChange,
            placeholder = stringResource(R.string.work_record_placeholder_optional),
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            modifier = Modifier.weight(0.8f)
        )
        // 删除按钮
        AppIconActionButton(
            onClick = onRemove,
            icon = Icons.Default.Delete,
            contentDescription = stringResource(R.string.work_record_color_delete_content_description),
            tint = MaterialTheme.colorScheme.error,
            containerColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 36.dp
        )
    }
}

@Composable
private fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val lineColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val textStyle = MaterialTheme.typography.bodySmall.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .drawBehind {
                val strokeWidth = if (isFocused) 2f else 1f
                drawLine(
                    color = lineColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = placeholderColor)
                    )
                }
                innerTextField()
            }
        }
    )
}
