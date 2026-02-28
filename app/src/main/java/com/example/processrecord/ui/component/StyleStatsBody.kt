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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import java.util.Locale

@Composable
fun PieChart(
    data: List<StyleStat>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    val total = data.sumOf { it.totalAmount }.takeIf { it > 0L } ?: 1L
    val totalYuan = total / 100.0
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    Canvas(modifier = modifier.padding(8.dp)) {
        var startAngle = -90f
        val canvasSize = size
        val diameter = minOf(canvasSize.width, canvasSize.height)
        val left = (canvasSize.width - diameter) / 2f
        val top = (canvasSize.height - diameter) / 2f

        data.forEachIndexed { index, stat ->
            val statAmountYuan = stat.totalAmount / 100.0
            val sweep = ((statAmountYuan / totalYuan) * 360.0).toFloat()
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
    colorItemsMap: Map<Long, List<WorkRecordColorItem>> = emptyMap(),
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
                text = stringResource(R.string.style_stats_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.style_stats_empty_subtitle),
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
                                text = pluralStringResource(
                                    R.plurals.style_stats_record_count,
                                    styleRecordCount,
                                    styleRecordCount
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                val amountYuan = stat.totalAmount / 100.0
                                Text(
                                    text = stringResource(
                                        R.string.style_stats_amount,
                                        String.format(Locale.getDefault(), "%.2f", amountYuan)
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (expanded) {
                                        stringResource(R.string.style_stats_collapse)
                                    } else {
                                        stringResource(R.string.style_stats_expand)
                                    },
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
                                    text = stringResource(R.string.style_stats_no_record_detail),
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
                                            colorItems = colorItemsMap[record.id] ?: emptyList(),
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
