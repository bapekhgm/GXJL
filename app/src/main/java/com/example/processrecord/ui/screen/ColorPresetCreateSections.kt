package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.ui.component.AppPrimaryButton
import com.example.processrecord.ui.component.AppSecondaryButton
import com.example.processrecord.ui.component.AppDropdownMenu
import com.example.processrecord.ui.component.AppDropdownMenuItem
import com.example.processrecord.data.entity.ColorGroup
import com.example.processrecord.ui.component.EnhancedTextField

@Composable
fun ColorPresetCreateSection(
    newName: String,
    groups: List<ColorGroup>,
    selectedGroupId: Long?,
    showGroupPicker: Boolean,
    previewHex: String,
    onNameChange: (String) -> Unit,
    onGroupPickerExpandedChange: (Boolean) -> Unit,
    onGroupSelected: (Long) -> Unit,
    onPickColorClick: () -> Unit,
    onSaveColorClick: () -> Unit
) {
    val previewColor = parseColorOrDefault(previewHex)

    ElevatedSectionCard(gradientBackground = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.color_preset_create_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            EnhancedTextField(
                value = newName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.color_preset_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                val selectedGroupName = groups.firstOrNull { it.id == selectedGroupId }?.name
                    ?: stringResource(R.string.color_group_select_placeholder)
                AppSecondaryButton(
                    text = stringResource(R.string.color_group_selected_value, selectedGroupName),
                    onClick = { onGroupPickerExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = groups.isNotEmpty()
                )

                AppDropdownMenu(
                    expanded = showGroupPicker,
                    onDismissRequest = { onGroupPickerExpandedChange(false) }
                ) {
                    groups.forEach { group ->
                        AppDropdownMenuItem(
                            text = group.name,
                            onClick = {
                                onGroupSelected(group.id)
                                onGroupPickerExpandedChange(false)
                            },
                            selected = group.id == selectedGroupId
                        )
                    }
                }
            }

            CompactCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        previewColor,
                                        previewColor.copy(alpha = 0.75f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.color_preset_preview_value, previewHex),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = groups.firstOrNull { it.id == selectedGroupId }?.name
                                ?: stringResource(R.string.color_group_select_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppSecondaryButton(
                    text = stringResource(R.string.color_preset_pick_color),
                    onClick = onPickColorClick,
                    modifier = Modifier.weight(1f)
                )
                AppPrimaryButton(
                    text = stringResource(R.string.color_preset_save_color),
                    onClick = onSaveColorClick,
                    modifier = Modifier.weight(1f),
                    enabled = newName.isNotBlank() && selectedGroupId != null
                )
            }
        }
    }
}

@Composable
fun ColorGroupManageSection(
    newGroupName: String,
    onNewGroupNameChange: (String) -> Unit,
    onAddGroupClick: () -> Unit
) {
    SectionCard {
        SectionHeader(title = stringResource(R.string.color_group_manage_title)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            EnhancedTextField(
                value = newGroupName,
                onValueChange = onNewGroupNameChange,
                label = { Text(stringResource(R.string.color_group_new_name_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            AppPrimaryButton(
                text = stringResource(R.string.color_group_add_button),
                onClick = onAddGroupClick,
                enabled = newGroupName.isNotBlank(),
                modifier = Modifier.align(Alignment.CenterVertically),
                height = 48.dp
            )
        }
    }
}
