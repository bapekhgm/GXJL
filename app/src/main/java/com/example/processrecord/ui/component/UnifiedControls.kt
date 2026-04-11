package com.example.processrecord.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.processrecord.R
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        ),
        contentPadding = contentPadding
    ) {
        AppButtonContent(text = text, icon = icon, tint = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        ),
        contentPadding = contentPadding
    ) {
        AppButtonContent(text = text, icon = icon, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AppDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f),
            contentColor = MaterialTheme.colorScheme.error,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        ),
        contentPadding = contentPadding
    ) {
        AppButtonContent(text = text, icon = icon, tint = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun AppButtonContent(
    text: String,
    icon: ImageVector?,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun AppFloatingButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun AppActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emphasized: Boolean = false
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (emphasized) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = contentColor
        )
    }
}

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 228.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.99f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
        ),
        content = content
    )
}

@Composable
fun AppDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    selected: Boolean = false
) {
    val titleColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        emphasized || selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val supportingColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        emphasized -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        else -> Color.Transparent
    }

    DropdownMenuItem(
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = titleColor
                )
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = supportingColor
                    )
                }
            }
        },
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 9.dp),
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        trailingIcon = trailingContent,
        colors = MenuDefaults.itemColors(
            textColor = titleColor,
            leadingIconColor = titleColor,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun AppIconActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
    enabled: Boolean = true,
    size: Dp = 34.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                if (enabled) {
                    containerColor
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                }
            )
            .border(
                width = 1.dp,
                color = if (enabled) borderColor else borderColor.copy(alpha = 0.45f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.55f),
            modifier = Modifier.size((size.value * 0.48f).dp)
        )
    }
}

@Composable
fun AppSelectableRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    selected: Boolean = false,
    emphasized: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        emphasized -> MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    val titleColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subtitleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = titleColor
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }
        }
        if (trailingContent != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = trailingContent
            )
        }
    }
}

@Composable
fun AppDialogScaffold(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    edgePaddingStart: Dp = 12.dp,
    edgePaddingEnd: Dp = 12.dp,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(start = edgePaddingStart, end = edgePaddingEnd),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.99f)
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.99f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!supportingText.isNullOrBlank()) {
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun AppTimePickerDialog(
    initialTimeMillis: Long,
    selectedDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val initialCalendar = remember(initialTimeMillis, selectedDateMillis) {
        java.util.Calendar.getInstance().apply {
            timeInMillis = if (initialTimeMillis > 0L) initialTimeMillis else selectedDateMillis
        }
    }
    var hourText by rememberSaveable(initialTimeMillis, selectedDateMillis) {
        mutableStateOf(initialCalendar.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0'))
    }
    var minuteText by rememberSaveable(initialTimeMillis, selectedDateMillis) {
        mutableStateOf(initialCalendar.get(java.util.Calendar.MINUTE).toString().padStart(2, '0'))
    }

    val hour = hourText.toIntOrNull()
    val minute = minuteText.toIntOrNull()
    val isValidTime = hour != null && minute != null && hour in 0..23 && minute in 0..59
    val dateText = remember(selectedDateMillis) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    AppDialogScaffold(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.record_time_picker_title),
        supportingText = stringResource(R.string.record_time_picker_supporting_text, dateText),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { input ->
                        val filtered = input.filter(Char::isDigit).take(2)
                        if (filtered.isEmpty() || filtered.toIntOrNull()?.let { it in 0..23 } == true) {
                            hourText = filtered
                        }
                    },
                    label = { Text(stringResource(R.string.record_time_picker_hour)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = AppTextFieldShape,
                    colors = appOutlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { input ->
                        val filtered = input.filter(Char::isDigit).take(2)
                        if (filtered.isEmpty() || filtered.toIntOrNull()?.let { it in 0..59 } == true) {
                            minuteText = filtered
                        }
                    },
                    label = { Text(stringResource(R.string.record_time_picker_minute)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = AppTextFieldShape,
                    colors = appOutlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        },
        actions = {
            AppSecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                height = 44.dp
            )
            AppPrimaryButton(
                text = stringResource(R.string.common_confirm),
                onClick = { onConfirm(hour ?: 0, minute ?: 0) },
                enabled = isValidTime,
                height = 44.dp
            )
        }
    )
}
