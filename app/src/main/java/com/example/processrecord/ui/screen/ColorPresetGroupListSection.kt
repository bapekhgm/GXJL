package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset

@Composable
fun ColorPresetGroupListSection(
    groups: List<ColorGroup>,
    presets: List<ColorPreset>,
    collapsedState: Map<Long, Boolean>,
    onToggleGroupCollapsed: (Long) -> Unit,
    onEditGroupClick: (ColorGroup) -> Unit,
    onDeleteGroupClick: (ColorGroup) -> Unit,
    onEditPresetClick: (ColorPreset) -> Unit,
    onDeletePresetClick: (ColorPreset) -> Unit
) {
    if (groups.isEmpty()) {
        Text(
            text = stringResource(R.string.color_preset_group_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    groups.sortedBy { it.sortOrder }.forEach { group ->
        val items = presets.filter { it.groupId == group.id }.sortedBy { it.sortOrder }
        val isCollapsed = collapsedState[group.id] ?: false

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggleGroupCollapsed(group.id) }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(
                            R.string.color_preset_toggle_group_content_description
                        )
                    )
                }
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.color_preset_group_count, items.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = { onEditGroupClick(group) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(
                            R.string.color_preset_edit_group_content_description
                        )
                    )
                }
                IconButton(onClick = { onDeleteGroupClick(group) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(
                            R.string.color_preset_delete_group_content_description
                        )
                    )
                }
            }
        }

        if (isCollapsed) {
            Spacer(modifier = Modifier.size(4.dp))
            return@forEach
        }

        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.color_preset_group_no_colors),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp)
            )
        } else {
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
                        Text(
                            stringResource(
                                R.string.color_preset_item_name_with_hex,
                                preset.name,
                                preset.hexValue
                            )
                        )
                    }
                    Row {
                        IconButton(onClick = { onEditPresetClick(preset) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(
                                    R.string.color_preset_edit_item_content_description
                                )
                            )
                        }
                        IconButton(onClick = { onDeletePresetClick(preset) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.color_preset_delete_item_content_description
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
