package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.viewmodel.ColorEntryUi
import com.example.processrecord.ui.viewmodel.WorkRecordDetails

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
                                Color(0xFFE53935),
                                Color(0xFF1E88E5),
                                Color(0xFF43A047),
                                Color(0xFFE53935)
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
            Button(
                onClick = { showAddColorSheet = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.work_record_button_add_color),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            OutlinedButton(
                onClick = onManageColorPresetsClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.work_record_button_manage_color_library),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (showAddColorSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddColorSheet = false
                    selectedColors.clear()
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.work_record_sheet_select_color_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (selectedColors.isNotEmpty()) {
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
                                Box(
                                    modifier = Modifier
                                        .background(
                                            parseColorOrDefault(preset.hexValue),
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.name,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(
                                                R.string.work_record_color_remove_content_description
                                            ),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { selectedColors.remove(preset) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val groupedFilteredPresets = colorGroups
                            .sortedBy { it.sortOrder }
                            .mapNotNull { group ->
                                val presets = colorPresets
                                    .filter { it.groupId == group.id }
                                    .sortedBy { it.sortOrder }
                                if (presets.isEmpty()) null else group to presets
                            }

                        if (groupedFilteredPresets.isNotEmpty()) {
                            groupedFilteredPresets.forEach { (group, presets) ->
                                val collapsed = sheetGroupCollapsed[group.id] ?: false
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
                                    TextButton(onClick = { sheetGroupCollapsed[group.id] = !collapsed }) {
                                        Text(
                                            text = stringResource(
                                                if (collapsed) {
                                                    R.string.work_record_sheet_expand
                                                } else {
                                                    R.string.work_record_sheet_collapse
                                                }
                                            )
                                        )
                                    }
                                }

                                if (collapsed) return@forEach

                                presets.forEach { preset ->
                                    val isSelected = selectedColors.any { it.id == preset.id }
                                    OutlinedButton(
                                        onClick = {
                                            if (!isSelected) selectedColors.add(preset)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isSelected
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(
                                                        color = parseColorOrDefault(preset.hexValue),
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.size(8.dp))
                                            Text(text = preset.name)
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = stringResource(
                                                        R.string.work_record_color_selected_content_description
                                                    ),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.work_record_sheet_no_matching_colors),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedColors.forEach { preset ->
                                    onAddColorEntryFromPreset(preset.name, preset.hexValue)
                                }
                                showAddColorSheet = false
                                selectedColors.clear()
                            },
                            enabled = selectedColors.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.work_record_button_done))
                        }
                        OutlinedButton(
                            onClick = {
                                showAddColorSheet = false
                                selectedColors.clear()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }

                    TextButton(
                        onClick = {
                            showAddColorSheet = false
                            selectedColors.clear()
                            onManageColorPresetsClick()
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(stringResource(R.string.work_record_button_go_manage_common_colors))
                    }
                }
            }
        }

        if (workRecordDetails.colorEntries.isNotEmpty()) {
            Text(
                text = stringResource(R.string.work_record_color_detail_list_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            workRecordDetails.colorEntries.forEach { entry ->
                ColorQuantityRow(
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
private fun ColorQuantityRow(
    entry: ColorEntryUi,
    onQuantityChange: (String) -> Unit,
    onDeficitChange: (String) -> Unit,
    onColorCodeChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(parseColorOrDefault(entry.colorHex), shape = RoundedCornerShape(50))
                )
                Text(text = entry.colorName, maxLines = 1, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = entry.quantity,
                onValueChange = { onQuantityChange(it.filter(Char::isDigit)) },
                label = { Text(stringResource(R.string.work_record_label_quantity)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.work_record_color_delete_content_description),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = entry.deficit,
                onValueChange = { onDeficitChange(it.filter(Char::isDigit)) },
                label = { Text(stringResource(R.string.work_record_label_deficit)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("") }
            )
            OutlinedTextField(
                value = entry.colorCode,
                onValueChange = onColorCodeChange,
                label = { Text(stringResource(R.string.work_record_label_color_code)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text(stringResource(R.string.work_record_placeholder_optional)) }
            )
        }
    }
}
