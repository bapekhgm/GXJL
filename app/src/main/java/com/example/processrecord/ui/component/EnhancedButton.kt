package com.example.processrecord.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.processrecord.ui.theme.AppAnimations
import com.example.processrecord.ui.theme.AppShapes

// ───────────────────────────────────────────────────────────
// 按钮尺寸枚举
// ───────────────────────────────────────────────────────────

enum class ButtonSize {
    Small, Medium, Large
}

// ───────────────────────────────────────────────────────────
// 渐变按钮 - 精美的渐变背景按钮
// ───────────────────────────────────────────────────────────

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    ),
    icon: ImageVector? = null,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Medium,
    shape: RoundedCornerShape = AppShapes.ButtonMedium,
    elevation: Dp = 4.dp,
    contentColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (height, horizontalPadding, verticalPadding, textStyle) = when (size) {
        ButtonSize.Small -> Tuple4(
            36.dp, 16.dp, 8.dp,
            MaterialTheme.typography.labelMedium
        )
        ButtonSize.Medium -> Tuple4(
            48.dp, 24.dp, 12.dp,
            MaterialTheme.typography.labelLarge
        )
        ButtonSize.Large -> Tuple4(
            56.dp, 32.dp, 16.dp,
            MaterialTheme.typography.titleMedium
        )
    }

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation / 2 else elevation,
        animationSpec = AppAnimations.fastTween(),
        label = "button_elevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "button_scale"
    )

    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .scale(animatedScale)
            .defaultMinSize(minHeight = height)
            .shadow(
                elevation = if (enabled) animatedElevation else 0.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(gradient, alpha = alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = textStyle.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
        }
    }
}

// ───────────────────────────────────────────────────────────
// 轮廓按钮 - 带边框的透明按钮
// ───────────────────────────────────────────────────────────

@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Medium,
    shape: RoundedCornerShape = AppShapes.ButtonMedium,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 2.dp,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (height, horizontalPadding, verticalPadding, textStyle) = when (size) {
        ButtonSize.Small -> Tuple4(
            36.dp, 16.dp, 8.dp,
            MaterialTheme.typography.labelMedium
        )
        ButtonSize.Medium -> Tuple4(
            48.dp, 24.dp, 12.dp,
            MaterialTheme.typography.labelLarge
        )
        ButtonSize.Large -> Tuple4(
            56.dp, 32.dp, 16.dp,
            MaterialTheme.typography.titleMedium
        )
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isPressed) borderColor.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = AppAnimations.fastTween(),
        label = "outlined_button_bg"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "outlined_button_scale"
    )

    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .scale(animatedScale)
            .defaultMinSize(minHeight = height)
            .clip(shape)
            .background(animatedBackgroundColor)
            .border(
                width = borderWidth,
                color = borderColor.copy(alpha = alpha),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = textStyle.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor.copy(alpha = alpha)
            )
        }
    }
}

// ───────────────────────────────────────────────────────────
// 辅助数据类
// ───────────────────────────────────────────────────────────

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
