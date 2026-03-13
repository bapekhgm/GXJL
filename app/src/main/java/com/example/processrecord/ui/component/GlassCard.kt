package com.example.processrecord.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.processrecord.ui.theme.AppAnimations
import com.example.processrecord.ui.theme.AppShapes
import com.example.processrecord.ui.theme.GlassBackgroundDark
import com.example.processrecord.ui.theme.GlassBackgroundLight
import com.example.processrecord.ui.theme.GlassBorderDark
import com.example.processrecord.ui.theme.GlassBorderLight
import com.example.processrecord.ui.theme.GlassLightMedium
import com.example.processrecord.ui.theme.GlassLightStrong
import com.example.processrecord.ui.theme.GlassBorderStrongLight
import com.example.processrecord.ui.theme.appGradients

// ───────────────────────────────────────────────────────────
// 玻璃拟态卡片 - 现代化毛玻璃效果
// ───────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = AppShapes.CardMedium,
    backgroundColor: Color = GlassBackgroundLight,
    borderColor: Color = GlassBorderLight,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    blurRadius: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation / 2 else elevation,
        animationSpec = AppAnimations.fastTween(),
        label = "glass_card_elevation"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = animatedElevation,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 玻璃拟态卡片 - 暗色主题版本
// ───────────────────────────────────────────────────────────

@Composable
fun GlassCardDark(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = AppShapes.CardMedium,
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        backgroundColor = GlassBackgroundDark,
        borderColor = GlassBorderDark,
        borderWidth = 1.5.dp,
        elevation = 6.dp,
        content = content
    )
}

// ───────────────────────────────────────────────────────────
// 高级玻璃卡片 - 带渐变叠加和光泽效果
// ───────────────────────────────────────────────────────────

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    gradientOverlay: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "premium_glass_scale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = AppAnimations.fastTween(),
        label = "premium_glass_elevation"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation,
                shape = shape,
                clip = false,
                spotColor = Color(0x1A1D4ED8)
            )
            .clip(shape)
            .background(GlassBackgroundLight)
            .then(
                if (gradientOverlay != null) {
                    Modifier.background(gradientOverlay)
                } else Modifier
            )
            .border(
                width = 1.5.dp,
                color = GlassBorderStrongLight,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        // 光泽层 - 顶部渐变
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 霓虹玻璃卡片 - 暗色主题高级效果
// ───────────────────────────────────────────────────────────

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    neonColor: Color = Color(0xFF8B5CF6),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "neon_glass_scale"
    )

    val glowAlpha = if (isPressed) 0.4f else 0.6f

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                clip = false,
                spotColor = neonColor.copy(alpha = glowAlpha)
            )
            .clip(shape)
            .background(Color(0x99101C2C))
            .border(
                width = 2.dp,
                color = neonColor.copy(alpha = 0.6f),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        // 内发光效果
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            neonColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 玻璃面板 - 用于分组内容
// ───────────────────────────────────────────────────────────

@Composable
fun GlassPanel(
    title: String? = null,
    modifier: Modifier = Modifier,
    showTopHighlight: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                spotColor = Color(0x141D4ED8)
            )
            .clip(shape)
            .background(GlassBackgroundLight)
            .border(
                width = 1.dp,
                color = GlassBorderLight,
                shape = shape
            )
    ) {
        // 顶部高光条
        if (showTopHighlight) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 标题
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        content()
    }
}

// ───────────────────────────────────────────────────────────
// 磨砂玻璃按钮
// ───────────────────────────────────────────────────────────

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "glass_button_scale"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(GlassLightMedium)
            .border(
                width = 1.dp,
                color = GlassBorderStrongLight,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        content()
    }
}
