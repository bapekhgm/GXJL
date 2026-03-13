package com.example.processrecord.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.processrecord.ui.theme.AppAnimations
import com.example.processrecord.ui.theme.AppShapes
import com.example.processrecord.ui.theme.appGradients
import kotlin.math.max

// ───────────────────────────────────────────────────────────
// 柱状图 - 简单的柱状图组件
// ───────────────────────────────────────────────────────────

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun SimpleBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    barWidth: Dp = 40.dp,
    barSpacing: Dp = 16.dp,
    showValues: Boolean = true,
    showLabels: Boolean = true
) {
    val actualMaxValue = maxValue ?: data.maxOfOrNull { it.value } ?: 1f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val animatedHeight by animateFloatAsState(
                    targetValue = item.value / actualMaxValue,
                    animationSpec = AppAnimations.slowTween(),
                    label = "bar_height_${item.label}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(barWidth + barSpacing)
                ) {
                    if (showValues) {
                        Text(
                            text = item.value.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(150.dp * animatedHeight)
                            .clip(AppShapes.TopRounded)
                            .background(item.color)
                    )

                    if (showLabels) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────────
// 渐变柱状图 - 带渐变效果的柱状图
// ───────────────────────────────────────────────────────────

data class GradientBarChartData(
    val label: String,
    val value: Float,
    val gradient: Brush
)

@Composable
fun GradientBarChart(
    data: List<GradientBarChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    barWidth: Dp = 40.dp,
    chartHeight: Dp = 150.dp
) {
    val actualMaxValue = maxValue ?: data.maxOfOrNull { it.value } ?: 1f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val animatedHeight by animateFloatAsState(
                    targetValue = item.value / actualMaxValue,
                    animationSpec = AppAnimations.slowTween(),
                    label = "gradient_bar_${item.label}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = item.value.toInt().toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(chartHeight * animatedHeight)
                            .clip(AppShapes.TopRounded)
                            .background(item.gradient)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────────
// 折线图 - 简单的折线图组件
// ───────────────────────────────────────────────────────────

data class LineChartData(
    val label: String,
    val value: Float
)

@Composable
fun SimpleLineChart(
    data: List<LineChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillGradient: Brush? = null,
    chartHeight: Dp = 150.dp,
    showPoints: Boolean = true
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val range = maxValue - minValue

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { index, item ->
            val x = index * spacing
            val normalizedValue = if (range > 0) (item.value - minValue) / range else 0.5f
            val y = height - (normalizedValue * height)
            Offset(x, y)
        }

        // 绘制填充区域
        if (fillGradient != null && points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, height)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(path, fillGradient)
        }

        // 绘制折线
        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4.dp.toPx()
                )
            }
        }

        // 绘制数据点
        if (showPoints) {
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

// ───────────────────────────────────────────────────────────
// 环形进度图 - 多段环形进度
// ───────────────────────────────────────────────────────────

data class RingSegment(
    val value: Float,
    val color: Color,
    val label: String
)

@Composable
fun RingChart(
    segments: List<RingSegment>,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 16.dp,
    showLegend: Boolean = true
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(size)) {
            var startAngle = -90f
            segments.forEach { segment ->
                val sweepAngle = (segment.value / total) * 360f
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth.toPx()
                    )
                )
                startAngle += sweepAngle
            }
        }

        if (showLegend) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                segments.forEach { segment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(segment.color)
                        )
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${((segment.value / total) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────────
// 高级环形图 - 带动画和中心内容
// ───────────────────────────────────────────────────────────

@Composable
fun PremiumRingChart(
    segments: List<RingSegment>,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 20.dp,
    centerContent: @Composable () -> Unit
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    
    // 预先计算动画值
    val animatedSegments = segments.map { segment ->
        val animatedSweep by animateFloatAsState(
            targetValue = (segment.value / total) * 360f,
            animationSpec = AppAnimations.slowTween(),
            label = "ring_segment_${segment.label}"
        )
        segment to animatedSweep
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size)
        ) {
            var currentAngle = -90f
            animatedSegments.forEach { (segment, animatedSweep) ->
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            segment.color,
                            segment.color.copy(alpha = 0.7f)
                        )
                    ),
                    startAngle = currentAngle,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )
                currentAngle += animatedSweep
            }
        }

        // 中心内容
        Box(
            modifier = Modifier.size(size - strokeWidth * 2 - 16.dp)
        ) {
            centerContent()
        }
    }
}

// ───────────────────────────────────────────────────────────
// 波浪图 - 用于显示趋势
// ───────────────────────────────────────────────────────────

@Composable
fun WaveChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    gradient: Brush = appGradients().primary,
    chartHeight: Dp = 120.dp,
    showFill: Boolean = true
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val normalizedValue = if (range > 0) (value - minValue) / range else 0.5f
            val y = height - (normalizedValue * height * 0.8f) - height * 0.1f
            Offset(x, y)
        }

        // 绘制填充区域
        if (showFill && points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(fillPath, gradient)
        }

        // 绘制波浪线
        if (points.size > 1) {
            val wavePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]
                    val midX = (current.x + next.x) / 2
                    quadraticBezierTo(
                        current.x, current.y,
                        midX, (current.y + next.y) / 2
                    )
                }
                lineTo(points.last().x, points.last().y)
            }
            drawPath(
                path = wavePath,
                color = Color.White.copy(alpha = 0.8f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

// ───────────────────────────────────────────────────────────
// 迷你图表 - 用于卡片中的小型展示
// ───────────────────────────────────────────────────────────

@Composable
fun MiniSparkline(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp,
    showDots: Boolean = false
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue

    Canvas(modifier = modifier.size(size)) {
        val width = size.toPx()
        val height = size.toPx()
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val normalizedValue = if (range > 0) (value - minValue) / range else 0.5f
            val y = height - (normalizedValue * height * 0.8f) - height * 0.1f
            Offset(x, y)
        }

        // 绘制渐变填充
        val gradientPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }
        drawPath(
            gradientPath,
            Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    color.copy(alpha = 0.05f)
                )
            )
        )

        // 绘制线条
        points.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }

        // 绘制端点
        if (showDots && points.isNotEmpty()) {
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = points.last()
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = points.last()
            )
        }
    }
}

// ───────────────────────────────────────────────────────────
// 数据卡片图表 - 带标题和数值的迷你图表
// ───────────────────────────────────────────────────────────

@Composable
fun DataCardChart(
    title: String,
    value: String,
    data: List<Float>,
    modifier: Modifier = Modifier,
    trend: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trend != null) {
                    Text(
                        text = if (trend >= 0) "+${(trend * 100).toInt()}%" else "${(trend * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (trend >= 0) {
                            Color(0xFF22C55E)
                        } else {
                            Color(0xFFEF4444)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            MiniSparkline(
                data = data,
                color = color,
                modifier = Modifier.fillMaxWidth().height(40.dp)
            )
        }
    }
}
