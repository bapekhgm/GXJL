package com.example.processrecord.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ───────────────────────────────────────────────────────────
// 区块标题组件 - 带渐变图标徽章
// ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    emphasized: Boolean = false,
    icon: @Composable () -> Unit
) {
    val iconShape = RoundedCornerShape(if (emphasized) 12.dp else 10.dp)
    val iconContainerColor = if (emphasized) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    }
    val iconBorderColor = if (emphasized) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(iconShape)
                .background(iconContainerColor)
                .border(width = 1.dp, color = iconBorderColor, shape = iconShape)
                .padding(if (emphasized) 8.dp else 7.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (emphasized) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                icon()
            }
        }
        Text(
            text = title,
            style = if (emphasized) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            },
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ───────────────────────────────────────────────────────────
// 区块卡片组件 - 玻璃拟态效果
// ───────────────────────────────────────────────────────────

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(if (emphasized) 22.dp else 20.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = shape,
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasized) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

// ───────────────────────────────────────────────────────────
// 高级卡片组件 - 带渐变边框和阴影
// ───────────────────────────────────────────────────────────

@Composable
fun ElevatedSectionCard(
    modifier: Modifier = Modifier,
    gradientBackground: Boolean = false,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = cardShape,
        border = BorderStroke(
            width = 1.dp,
            color = if (gradientBackground) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gradientBackground) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            content()
        }
    }
}

// ───────────────────────────────────────────────────────────
// 强调卡片组件 - 用于突出显示重要内容
// ───────────────────────────────────────────────────────────

@Composable
fun AccentCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.22f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 14.dp, top = 18.dp, bottom = 18.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(accentColor.copy(alpha = 0.9f))
                    .width(4.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 18.dp, end = 20.dp, bottom = 18.dp)
            ) {
                content()
            }
        }
    }
}

// ───────────────────────────────────────────────────────────
// 小型卡片组件 - 用于紧凑布局
// ───────────────────────────────────────────────────────────

@Composable
fun CompactCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .animateContentSize()
        .then(
            if (onClick != null) {
                Modifier.clip(RoundedCornerShape(16.dp))
            } else {
                Modifier
            }
        )

    if (onClick != null) {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                content()
            }
        }
    }
}
