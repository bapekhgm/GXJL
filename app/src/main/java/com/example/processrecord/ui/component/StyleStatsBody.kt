package com.example.processrecord.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.processrecord.R
import com.example.processrecord.data.dao.StyleStat
import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.WorkRecordColorItem
import com.example.processrecord.ui.viewmodel.MonthlyStyleStatsSection
import java.util.Locale

@Composable
fun StyleStatsBody(
    styleStats: List<StyleStat>,
    workRecordList: List<WorkRecord>,
    colorItemsMap: Map<Long, List<WorkRecordColorItem>> = emptyMap(),
    onRecordClick: (Long) -> Unit,
    topContentPadding: Dp = 10.dp,
    bottomContentPadding: Dp = 112.dp,
    modifier: Modifier = Modifier
) {
    if (styleStats.isEmpty()) {
        StyleStatsEmptyState(
            title = stringResource(R.string.style_stats_empty_title),
            subtitle = stringResource(R.string.style_stats_empty_subtitle),
            modifier = modifier
        )
        return
    }

    val recordsByStyle = remember(workRecordList) {
        workRecordList.groupBy { it.style }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topContentPadding,
            bottom = bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = styleStats, key = { it.style }) { stat ->
            StyleStatCard(
                stat = stat,
                styleRecords = recordsByStyle[stat.style].orEmpty(),
                colorItemsMap = colorItemsMap,
                onRecordClick = onRecordClick,
                expansionKey = "selected_month_${stat.style}"
            )
        }
    }
}

@Composable
fun MonthlyStyleStatsBody(
    monthSections: List<MonthlyStyleStatsSection>,
    colorItemsMap: Map<Long, List<WorkRecordColorItem>> = emptyMap(),
    onRecordClick: (Long) -> Unit,
    topContentPadding: Dp = 10.dp,
    bottomContentPadding: Dp = 112.dp,
    modifier: Modifier = Modifier
) {
    if (monthSections.isEmpty()) {
        StyleStatsEmptyState(
            title = stringResource(R.string.style_stats_empty_title),
            subtitle = stringResource(R.string.home_month_stats_empty_subtitle),
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topContentPadding,
            bottom = bottomContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items = monthSections, key = { it.monthStart }) { section ->
            MonthSectionCard(
                section = section,
                colorItemsMap = colorItemsMap,
                onRecordClick = onRecordClick
            )
        }
    }
}

@Composable
fun StyleStatsSummaryCard(
    styleStats: List<StyleStat>,
    modifier: Modifier = Modifier
) {
    val totalQuantity = styleStats.sumOf { it.totalQuantity }
    val totalAmount = styleStats.sumOf { it.totalAmount } / 100.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.home_tab_month_overview),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.style_stats_summary_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StyleStatsMetricChip(
                        label = stringResource(R.string.style_stats_summary_total_label),
                        value = stringResource(
                            R.string.style_stats_amount,
                            String.format(Locale.getDefault(), "%.2f", totalAmount)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    StyleStatsMetricChip(
                        label = stringResource(R.string.style_stats_summary_quantity_label),
                        value = String.format(Locale.getDefault(), "%,d", totalQuantity),
                        modifier = Modifier.weight(1f)
                    )
                }
                StyleStatsMetricChip(
                    label = stringResource(R.string.style_stats_summary_style_label),
                    value = pluralStringResource(
                        R.plurals.style_stats_style_count,
                        styleStats.size,
                        styleStats.size
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StyleStatsEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthSectionCard(
    section: MonthlyStyleStatsSection,
    colorItemsMap: Map<Long, List<WorkRecordColorItem>>,
    onRecordClick: (Long) -> Unit
) {
    val recordsByStyle = remember(section.records) {
        section.records.groupBy { it.style }
    }
    val totalAmount = section.totalAmount / 100.0
    val recordCount = section.records.size

    Card(
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.home_month_stats_month_title,
                            section.year,
                            section.month
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.style_stats_record_count,
                            recordCount,
                            recordCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(
                        R.string.style_stats_amount,
                        String.format(Locale.getDefault(), "%.2f", totalAmount)
                    ),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StyleStatsMetricChip(
                    label = stringResource(R.string.style_stats_summary_quantity_label),
                    value = String.format(Locale.getDefault(), "%,d", section.totalQuantity),
                    modifier = Modifier.weight(1f)
                )
                StyleStatsMetricChip(
                    label = stringResource(R.string.style_stats_summary_record_label),
                    value = pluralStringResource(
                        R.plurals.style_stats_record_count,
                        recordCount,
                        recordCount
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            StyleStatsMetricChip(
                label = stringResource(R.string.style_stats_summary_style_label),
                value = pluralStringResource(
                    R.plurals.style_stats_style_count,
                    section.styleStats.size,
                    section.styleStats.size
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                section.styleStats.forEach { stat ->
                    StyleStatCard(
                        stat = stat,
                        styleRecords = recordsByStyle[stat.style].orEmpty(),
                        colorItemsMap = colorItemsMap,
                        onRecordClick = onRecordClick,
                        expansionKey = "month_${section.year}_${section.month}_${stat.style}"
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleStatCard(
    stat: StyleStat,
    styleRecords: List<WorkRecord>,
    colorItemsMap: Map<Long, List<WorkRecordColorItem>>,
    onRecordClick: (Long) -> Unit,
    expansionKey: String
) {
    var expanded by rememberSaveable(expansionKey) { mutableStateOf(false) }
    val styleRecordCount = styleRecords.size
    val amountYuan = stat.totalAmount / 100.0

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stat.style,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                RoundedCornerShape(999.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.style_stats_record_count,
                                styleRecordCount,
                                styleRecordCount
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.style_stats_amount,
                            String.format(Locale.getDefault(), "%.2f", amountYuan)
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (expanded) {
                            stringResource(R.string.style_stats_collapse)
                        } else {
                            stringResource(R.string.style_stats_expand)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Divider(
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (styleRecords.isEmpty()) {
                        Text(
                            text = stringResource(R.string.style_stats_no_record_detail),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
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
        }
    }
}

@Composable
private fun StyleStatsMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                RoundedCornerShape(18.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
