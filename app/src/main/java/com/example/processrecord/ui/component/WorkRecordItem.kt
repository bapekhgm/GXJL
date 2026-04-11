package com.example.processrecord.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.processrecord.R
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ───────────────────────────────────────────────────────────
// 辅助函数
// ───────────────────────────────────────────────────────────

internal fun parseHexColor(hex: String): Color? = try {
    Color(hex.toColorInt())
} catch (_: Exception) {
    null
}

internal fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.6f
}

// ───────────────────────────────────────────────────────────
// 工作记录卡片组件 - 精美样式
// ───────────────────────────────────────────────────────────

enum class WorkRecordItemDisplayMode {
    Standard,
    LargeDaily,
    GroupEmbedded,
    GroupDetail
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkRecordItem(
    record: WorkRecord,
    colorItems: List<WorkRecordColorItem> = emptyList(),
    onClick: (() -> Unit)? = null,
    displayMode: WorkRecordItemDisplayMode = WorkRecordItemDisplayMode.Standard,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isLargeDaily = displayMode == WorkRecordItemDisplayMode.LargeDaily
    val isGroupEmbedded = displayMode == WorkRecordItemDisplayMode.GroupEmbedded
    val isGroupDetail = displayMode == WorkRecordItemDisplayMode.GroupDetail
    val showStyleHeader = displayMode == WorkRecordItemDisplayMode.Standard ||
        displayMode == WorkRecordItemDisplayMode.LargeDaily
    val showInlineAmountBadge = isGroupEmbedded || isGroupDetail

    fun fmtAmount(cents: Long): String {
        val yuan = cents / 100.0
        return String.format(Locale.getDefault(), "%.2f", yuan)
    }

    fun fmtQty(value: Long): String = value.toString()

    // 交互状态
    val interactionSource = remember { MutableInteractionSource() }

    val cardShape = when (displayMode) {
        WorkRecordItemDisplayMode.LargeDaily -> RoundedCornerShape(22.dp)
        WorkRecordItemDisplayMode.GroupEmbedded -> RoundedCornerShape(18.dp)
        WorkRecordItemDisplayMode.GroupDetail -> RoundedCornerShape(24.dp)
        WorkRecordItemDisplayMode.Standard -> RoundedCornerShape(24.dp)
    }
    val contentHorizontalPadding = when (displayMode) {
        WorkRecordItemDisplayMode.LargeDaily -> 18.dp
        WorkRecordItemDisplayMode.GroupEmbedded -> 14.dp
        WorkRecordItemDisplayMode.GroupDetail -> 20.dp
        WorkRecordItemDisplayMode.Standard -> 20.dp
    }
    val contentVerticalPadding = when (displayMode) {
        WorkRecordItemDisplayMode.LargeDaily -> 14.dp
        WorkRecordItemDisplayMode.GroupEmbedded -> 12.dp
        WorkRecordItemDisplayMode.GroupDetail -> 16.dp
        WorkRecordItemDisplayMode.Standard -> 16.dp
    }
    val leadingIconSize = if (isLargeDaily) 40.dp else 36.dp
    val leadingItemSpacing = if (isLargeDaily) 8.dp else 6.dp
    val amountBadgeShape = when (displayMode) {
        WorkRecordItemDisplayMode.LargeDaily -> RoundedCornerShape(18.dp)
        WorkRecordItemDisplayMode.GroupEmbedded -> RoundedCornerShape(14.dp)
        WorkRecordItemDisplayMode.GroupDetail -> RoundedCornerShape(16.dp)
        WorkRecordItemDisplayMode.Standard -> RoundedCornerShape(16.dp)
    }
    val amountHorizontalPadding = when (displayMode) {
        WorkRecordItemDisplayMode.LargeDaily -> 16.dp
        WorkRecordItemDisplayMode.GroupEmbedded -> 12.dp
        WorkRecordItemDisplayMode.GroupDetail -> 14.dp
        WorkRecordItemDisplayMode.Standard -> 14.dp
    }
    val amountVerticalPadding = when (displayMode) {
        WorkRecordItemDisplayMode.LargeDaily -> 9.dp
        WorkRecordItemDisplayMode.GroupEmbedded -> 7.dp
        WorkRecordItemDisplayMode.GroupDetail -> 8.dp
        WorkRecordItemDisplayMode.Standard -> 8.dp
    }
    val styleTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.titleLarge.copy(
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold
        )
    } else {
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        )
    }
    val serialTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.labelMedium
    } else if (isGroupEmbedded || isGroupDetail) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.labelSmall
    }
    val amountTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.titleLarge.copy(
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    } else if (isGroupEmbedded) {
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        )
    } else if (isGroupDetail) {
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold
        )
    } else {
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold
        )
    }
    val processTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold
        )
    } else if (isGroupEmbedded || isGroupDetail) {
        MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold
        )
    } else {
        MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold
        )
    }
    val quantityTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium
        )
    } else if (isGroupEmbedded || isGroupDetail) {
        MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium
        )
    } else {
        MaterialTheme.typography.bodySmall
    }
    val metadataTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.labelMedium
    } else if (isGroupEmbedded || isGroupDetail) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.labelSmall
    }
    val remarkTextStyle = if (isLargeDaily) {
        MaterialTheme.typography.bodySmall
    } else if (isGroupEmbedded || isGroupDetail) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.labelSmall
    }
    val showTotalQuantity = record.totalQuantity > 0 && record.totalQuantity != record.quantity
    val quantityPriceText = if (record.quantity > 0) {
        if (record.unitPrice > 0) {
            stringResource(
                R.string.work_record_item_quantity_times_price,
                fmtQty(record.quantity),
                fmtAmount(record.unitPrice)
            )
        } else {
            fmtQty(record.quantity)
        }
    } else {
        null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = cardShape,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGroupEmbedded) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = contentHorizontalPadding,
                vertical = contentVerticalPadding
            )
        ) {
            if (showStyleHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(leadingItemSpacing)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(leadingIconSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.style,
                                style = styleTextStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (record.serialNumber.isNotBlank()) {
                                Text(
                                    text = record.serialNumber,
                                    style = serialTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(amountBadgeShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = amountBadgeShape
                            )
                            .padding(horizontal = amountHorizontalPadding, vertical = amountVerticalPadding)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.work_record_value_amount,
                                fmtAmount(record.amount)
                            ),
                            style = amountTextStyle,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = record.processName,
                        style = processTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (showInlineAmountBadge) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(amountBadgeShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = amountBadgeShape
                            )
                            .padding(horizontal = amountHorizontalPadding, vertical = amountVerticalPadding)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.work_record_value_amount,
                                fmtAmount(record.amount)
                            ),
                            style = amountTextStyle,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else if (quantityPriceText != null) {
                    Text(
                        text = quantityPriceText,
                        style = quantityTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showInlineAmountBadge && (quantityPriceText != null || record.serialNumber.isNotBlank())) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (quantityPriceText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = quantityPriceText,
                                style = quantityTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (record.serialNumber.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = record.serialNumber,
                                style = serialTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (showTotalQuantity) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.work_record_item_total_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Text(
                                text = fmtQty(record.totalQuantity),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ───────────────────────────────────────────────
            // 颜色明细
            // ───────────────────────────────────────────────
            val hasColorItems = colorItems.isNotEmpty()
            val hasColorText = record.color.isNotBlank()
            
            if (hasColorItems || hasColorText) {
                Spacer(modifier = Modifier.height(10.dp))

                if (hasColorItems) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        colorItems.forEach { item ->
                            val bgColor = parseHexColor(item.colorHex)
                                ?: MaterialTheme.colorScheme.secondaryContainer
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 颜色方块
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(bgColor)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(5.dp)
                                        )
                                )
                                
                                // 颜色名称
                                Text(
                                    text = item.colorName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                // 数量徽章
                                if (item.quantity > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = fmtQty(item.quantity),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // 欠数徽章
                                val deficitText = item.deficit.trim()
                                if (deficitText.isNotBlank()) {
                                    val isResolved = item.isDeficitResolved
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isResolved) {
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                } else {
                                                    MaterialTheme.colorScheme.errorContainer
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.work_record_item_deficit_value,
                                                deficitText
                                            ),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isResolved) {
                                                    FontWeight.Medium
                                                } else {
                                                    FontWeight.Bold
                                                },
                                                textDecoration = if (isResolved) {
                                                    TextDecoration.LineThrough
                                                } else {
                                                    TextDecoration.None
                                                }
                                            ),
                                            color = if (isResolved) {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            } else {
                                                MaterialTheme.colorScheme.error
                                            }
                                        )
                                    }
                                }
                                
                                // 色号
                                if (!item.colorCode.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.work_record_item_color_code_value,
                                                item.colorCode
                                            ),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 简单颜色列表
                    val colorList = record.color
                        .split(Regex("\\s+"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // ───────────────────────────────────────────────
            // 时间信息
            // ───────────────────────────────────────────────
            if (record.startTime > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                    style = metadataTextStyle,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // ───────────────────────────────────────────────
            // 备注
            // ───────────────────────────────────────────────
            if (record.remark.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                if (record.startTime <= 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = stringResource(R.string.work_record_item_remark_prefix, record.remark),
                    style = remarkTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
