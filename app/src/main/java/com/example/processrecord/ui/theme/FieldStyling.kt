package com.example.processrecord.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ───────────────────────────────────────────────────────────
// 表单字段形状配置
// ───────────────────────────────────────────────────────────

val AppTextFieldShape = RoundedCornerShape(18.dp)
val AppSmallTextFieldShape = RoundedCornerShape(14.dp)
val AppLargeTextFieldShape = RoundedCornerShape(24.dp)

// ───────────────────────────────────────────────────────────
// 输入框颜色配置
// ───────────────────────────────────────────────────────────

@Composable
fun appOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f),
    
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    errorBorderColor = MaterialTheme.colorScheme.error,
    
    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    errorLabelColor = MaterialTheme.colorScheme.error,
    
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    errorLeadingIconColor = MaterialTheme.colorScheme.error,
    errorTrailingIconColor = MaterialTheme.colorScheme.error,
    
    cursorColor = MaterialTheme.colorScheme.primary,
    errorCursorColor = MaterialTheme.colorScheme.error,
    
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    
    focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
    unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
    errorSupportingTextColor = MaterialTheme.colorScheme.error
)

// ───────────────────────────────────────────────────────────
// 动画高度
// ───────────────────────────────────────────────────────────

@Composable
fun animatedTextFieldElevation(
    interactionSource: InteractionSource,
    defaultElevation: Dp = 1.dp,
    focusedElevation: Dp = 4.dp
): Dp {
    val isFocused by interactionSource.collectIsFocusedAsState()
    return animateDpAsState(
        targetValue = if (isFocused) focusedElevation else defaultElevation,
        animationSpec = tween(durationMillis = 200),
        label = "textFieldElevation"
    ).value
}

// ───────────────────────────────────────────────────────────
// 卡片容器颜色
// ───────────────────────────────────────────────────────────

@Composable
fun cardContainerColor(): Color {
    return MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
}

@Composable
fun elevatedCardContainerColor(): Color {
    return MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
}

// ───────────────────────────────────────────────────────────
// 边框颜色
// ───────────────────────────────────────────────────────────

@Composable
fun subtleBorderColor(): Color {
    return MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
}

@Composable
fun accentBorderColor(): Color {
    return MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
}
