package com.example.processrecord.ui.component

import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 解析 hex 颜色字符串，失败时返回 null */
internal fun parseHexColor(hex: String): Color? = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) { null }

/** 判断颜色是否偏亮，用于决定文字颜色 */
internal fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.6f
}

@Composable
fun WorkRecordItem(
    record: WorkRecord,
    colorItems: List<WorkRecordColorItem> = emptyList(),
    onCopy: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    fun fmtQty(v: Double) = if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(v)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // 第一行：款号 + 序号 + 金额
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
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "¥ ${String.format(Locale.getDefault(), "%.2f", record.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 第二行：工序 + 数量×单价 + 总数量
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.processName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                if (record.quantity > 0) {
                    Text(
                        text = buildString {
                            append(fmtQty(record.quantity))
                            if (record.unitPrice > 0) append(" × ¥${fmtQty(record.unitPrice)}")
                        },
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
                        Text("总", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(fmtQty(record.totalQuantity), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 第三行：颜色明细
            val hasColorItems = colorItems.isNotEmpty()
            val hasColorText = record.color.isNotBlank()
            if (hasColorItems || hasColorText) {
                Spacer(modifier = Modifier.height(6.dp))
                if (hasColorItems) {
                    FlowRow(mainAxisSpacing = 5.dp, crossAxisSpacing = 5.dp) {
                        colorItems.forEach { item ->
                            val bgColor = parseHexColor(item.colorHex) ?: MaterialTheme.colorScheme.secondaryContainer
                            val textColor = if (bgColor.isLight()) Color(0xFF212121) else Color.White
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.background(bgColor, RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(item.colorName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = textColor)
                                if (item.quantity > 0) {
                                    Text(" · ${fmtQty(item.quantity)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = textColor.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                } else {
                    val colorList = record.color.split(Regex("\\s+")).map { it.trim() }.filter { it.isNotEmpty() }
                    FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
                        colorList.forEach { colorItem ->
                            val regex = Regex("([\\u4e00-\\u9fa5A-Za-z]+)(\\d+(?:\\.\\d+)?)?")
                            val match = regex.find(colorItem)
                            val name = match?.groupValues?.get(1) ?: colorItem
                            val qty = match?.groupValues?.getOrNull(2)?.takeIf { it.isNotBlank() }
                            Text(
                                text = if (qty != null) "$name·$qty" else name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 时间行
            if (record.startTime > 0) {
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormat2 = SimpleDateFormat("MM/dd", Locale.getDefault())
                val startDateStr = dateFormat2.format(Date(record.startTime))
                val startTimeStr = timeFormat.format(Date(record.startTime))
                val endStr = if (record.endTime > 0) {
                    val endDateStr = dateFormat2.format(Date(record.endTime))
                    val endTimeStr = timeFormat.format(Date(record.endTime))
                    if (endDateStr != startDateStr) "$endDateStr $endTimeStr" else endTimeStr
                } else ""
                val durText = if (record.endTime > record.startTime) {
                    val ms = record.endTime - record.startTime
                    val d = ms / 86400000L; val h = (ms % 86400000L) / 3600000L; val m = (ms % 3600000L) / 60000L
                    buildString { append("  "); if (d > 0) append("${d}天"); if (h > 0) append("${h}h"); append("${m}m") }
                } else ""
                Text(
                    text = "⏱  $startDateStr $startTimeStr" + (if (endStr.isNotEmpty()) " – $endStr" else "") + durText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // 备注行
            if (record.remark.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                if (record.startTime <= 0) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text("💬  ${record.remark}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            // 复制按钮
            if (onCopy != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCopy, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("复制此记录", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}