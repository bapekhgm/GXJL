package com.example.processrecord.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.processrecord.R
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun parseHexColor(hex: String): Color? = try {
    Color(hex.toColorInt())
} catch (_: Exception) {
    null
}

internal fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.6f
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkRecordItem(
    record: WorkRecord,
    colorItems: List<WorkRecordColorItem> = emptyList(),
    onCopy: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun fmtAmount(cents: Long): String {
        val yuan = cents / 100.0
        return String.format(Locale.getDefault(), "%.2f", yuan)
    }

    fun fmtQty(value: Long): String = value.toString()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${record.style}#",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (record.serialNumber.isNotBlank()) {
                        Text(
                            text = record.serialNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.work_record_value_amount, fmtAmount(record.amount)),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.processName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE53935),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                if (record.quantity > 0) {
                    val quantityPriceText = if (record.unitPrice > 0) {
                        stringResource(
                            R.string.work_record_item_quantity_times_price,
                            fmtQty(record.quantity),
                            fmtAmount(record.unitPrice)
                        )
                    } else {
                        fmtQty(record.quantity)
                    }

                    Text(
                        text = quantityPriceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (record.totalQuantity > 0 && record.totalQuantity != record.quantity) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.work_record_item_total_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = fmtQty(record.totalQuantity),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            val hasColorItems = colorItems.isNotEmpty()
            val hasColorText = record.color.isNotBlank()
            if (hasColorItems || hasColorText) {
                Spacer(modifier = Modifier.height(6.dp))
                if (hasColorItems) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        colorItems.forEach { item ->
                            val bgColor = parseHexColor(item.colorHex)
                                ?: MaterialTheme.colorScheme.secondaryContainer
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(bgColor, RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = item.colorName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.quantity > 0) {
                                    Text(
                                        text = fmtQty(item.quantity),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                if (item.deficit > 0) {
                                    Text(
                                        text = stringResource(
                                            R.string.work_record_item_deficit_value,
                                            fmtQty(item.deficit)
                                        ),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFFF6B6B),
                                        modifier = Modifier
                                            .background(Color(0xFFFFE5E5), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                if (!item.colorCode.isNullOrBlank()) {
                                    Text(
                                        text = stringResource(
                                            R.string.work_record_item_color_code_value,
                                            item.colorCode
                                        ),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val colorList = record.color
                        .split(Regex("\\s+"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        colorList.forEach { colorItem ->
                            val regex = Regex("([\\u4e00-\\u9fa5A-Za-z]+)(\\d+(?:\\.\\d+)?)?")
                            val match = regex.find(colorItem)
                            val name = match?.groupValues?.get(1) ?: colorItem
                            val qty = match?.groupValues?.getOrNull(2)?.takeIf { it.isNotBlank() }
                            val text = if (qty != null) {
                                stringResource(R.string.work_record_item_color_chip_with_qty, name, qty)
                            } else {
                                name
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (record.startTime > 0) {
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                val startDateStr = dateFormat.format(Date(record.startTime))
                val startTimeStr = timeFormat.format(Date(record.startTime))
                val endStr = if (record.endTime > 0) {
                    val endDateStr = dateFormat.format(Date(record.endTime))
                    val endTimeStr = timeFormat.format(Date(record.endTime))
                    if (endDateStr != startDateStr) "$endDateStr $endTimeStr" else endTimeStr
                } else {
                    ""
                }
                val durationText = if (record.endTime > record.startTime) {
                    val ms = record.endTime - record.startTime
                    val d = ms / 86_400_000L
                    val h = (ms % 86_400_000L) / 3_600_000L
                    val m = (ms % 3_600_000L) / 60_000L
                    buildString {
                        if (d > 0) append(stringResource(R.string.work_record_duration_day_part, d))
                        if (h > 0) append(stringResource(R.string.work_record_duration_hour_part, h))
                        append(stringResource(R.string.work_record_duration_minute_part, m))
                    }.trim()
                } else {
                    ""
                }

                val timeLineText = buildString {
                    append(stringResource(R.string.work_record_item_time_prefix))
                    append(" ")
                    append(startDateStr)
                    append(" ")
                    append(startTimeStr)
                    if (endStr.isNotEmpty()) {
                        append(" - ")
                        append(endStr)
                    }
                    if (durationText.isNotEmpty()) {
                        append("  ")
                        append(durationText)
                    }
                }

                Text(
                    text = timeLineText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (record.remark.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                if (record.startTime <= 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = stringResource(R.string.work_record_item_remark_prefix, record.remark),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onCopy != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCopy,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.work_record_item_copy_record),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

