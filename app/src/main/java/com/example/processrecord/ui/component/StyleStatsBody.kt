package com.example.processrecord.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import java.util.Locale

@Composable
fun PieChart(
    data: List<StyleStat>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    val total = data.sumOf { it.totalAmount }.takeIf { it > 0.0 } ?: 1.0
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    Canvas(modifier = modifier.padding(8.dp)) {
        var startAngle = -90f
        val canvasSize = this.size
        val diameter = minOf(canvasSize.width, canvasSize.height)
        val left = (canvasSize.width - diameter) / 2f
        val top = (canvasSize.height - diameter) / 2f

        data.forEachIndexed { index, stat ->
            val sweep = ((stat.totalAmount / total) * 360.0).toFloat()
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(left, top),
                size = Size(diameter, diameter)
            )
            startAngle += sweep
        }
    }
}

@Composable
fun StyleStatsBody(
    styleStats: List<StyleStat>,
    workRecordList: List<WorkRecord>,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (styleStats.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "本月暂无统计数据",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "快去记一笔吧！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items = styleStats) { stat ->
                var expanded by remember { mutableStateOf(false) }

                Column {
                    val styleRecordCount = workRecordList.count { it.style == stat.style }
                    ListItem(
                        headlineContent = { Text(stat.style, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            Text(
                                text = "${styleRecordCount} 条记录",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥${String.format(Locale.getDefault(), "%.2f", stat.totalAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (expanded) "收起 ▲" else "展开 ▼",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        modifier = Modifier.clickable { expanded = !expanded }
                    )

                    AnimatedVisibility(visible = expanded) {
                        Column {
                            val styleRecords = workRecordList.filter { it.style == stat.style }
                            if (styleRecords.isEmpty()) {
                                Text(
                                    text = "无记录详情",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    styleRecords.forEach { record ->
                                        WorkRecordItem(
                                            record = record,
                                            onClick = { onRecordClick(record.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}