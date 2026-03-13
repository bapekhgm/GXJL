package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.data.entity.ColorPreset
import com.example.processrecord.ui.component.EmptyStateCard
import com.example.processrecord.ui.component.AppIconActionButton
import com.example.processrecord.ui.component.AppSelectableRow

fun LazyListScope.ColorPresetGroupListSection(
    groups: List<ColorGroup>,
    presetsByGroupId: Map<Long, List<ColorPreset>>,
    collapsedState: Map<Long, Boolean>,
    onToggleGroupCollapsed: (Long) -> Unit,
    onEditGroupClick: (ColorGroup) -> Unit,
    onDeleteGroupClick: (ColorGroup) -> Unit,
    onEditPresetClick: (ColorPreset) -> Unit,
    onDeletePresetClick: (ColorPreset) -> Unit
) {
    if (groups.isEmpty()) {
        item(key = "color_groups_empty") {
            EmptyStateCard(
                title = stringResource(R.string.color_preset_saved_title),
                subtitle = stringResource(R.string.color_preset_group_empty),
                icon = Icons.Default.Info,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    items(
        items = groups.sortedBy { it.sortOrder },
        key = { it.id }
    ) { group ->
        ColorPresetGroupCard(
            group = group,
            presets = presetsByGroupId[group.id].orEmpty(),
            isCollapsed = collapsedState[group.id] ?: false,
            onToggleGroupCollapsed = onToggleGroupCollapsed,
            onEditGroupClick = onEditGroupClick,
            onDeleteGroupClick = onDeleteGroupClick,
            onEditPresetClick = onEditPresetClick,
            onDeletePresetClick = onDeletePresetClick
        )
    }
}

@Composable
private fun ColorPresetGroupCard(
    group: ColorGroup,
    presets: List<ColorPreset>,
    isCollapsed: Boolean,
    onToggleGroupCollapsed: (Long) -> Unit,
    onEditGroupClick: (ColorGroup) -> Unit,
    onDeleteGroupClick: (ColorGroup) -> Unit,
    onEditPresetClick: (ColorPreset) -> Unit,
    onDeletePresetClick: (ColorPreset) -> Unit
) {
    CompactCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppSelectableRow(
                title = group.name,
                subtitle = if (presets.isNotEmpty()) {
                    stringResource(R.string.color_preset_group_count, presets.size)
                } else {
                    stringResource(R.string.color_preset_group_no_colors)
                },
                onClick = { onToggleGroupCollapsed(group.id) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(
                                R.string.color_preset_toggle_group_content_description
                            ),
                            modifier = Modifier.rotate(if (isCollapsed) -90f else 0f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingContent = {
                    AppIconActionButton(
                        onClick = { onEditGroupClick(group) },
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(
                            R.string.color_preset_edit_group_content_description
                        ),
                        size = 34.dp
                    )
                    AppIconActionButton(
                        onClick = { onDeleteGroupClick(group) },
                        icon = Icons.Default.Delete,
                        contentDescription = stringResource(
                            R.string.color_preset_delete_group_content_description
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.24f),
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                        size = 34.dp
                    )
                }
            )

            if (!isCollapsed) {
                if (presets.isEmpty()) {
                    Text(
                        text = stringResource(R.string.color_preset_group_no_colors),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 44.dp)
                    )
                } else {
                    presets.forEach { preset ->
                        AppSelectableRow(
                            title = preset.name,
                            subtitle = preset.hexValue,
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(parseColorOrDefault(preset.hexValue), CircleShape)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                )
                            },
                            trailingContent = {
                                AppIconActionButton(
                                    onClick = { onEditPresetClick(preset) },
                                    icon = Icons.Default.Edit,
                                    contentDescription = stringResource(
                                        R.string.color_preset_edit_item_content_description
                                    ),
                                    size = 34.dp
                                )
                                AppIconActionButton(
                                    onClick = { onDeletePresetClick(preset) },
                                    icon = Icons.Default.Delete,
                                    contentDescription = stringResource(
                                        R.string.color_preset_delete_item_content_description
                                    ),
                                    tint = MaterialTheme.colorScheme.error,
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.24f),
                                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                                    size = 34.dp
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
