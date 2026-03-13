package com.example.processrecord.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.processrecord.ui.theme.AppAnimations
import com.example.processrecord.ui.theme.AppShapes

// ───────────────────────────────────────────────────────────
// 可按压卡片 - 带按压动画效果
// ───────────────────────────────────────────────────────────

@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = AppShapes.CardMedium,
    backgroundColor: Color = Color.White,
    elevation: Dp = 4.dp,
    pressScale: Float = 0.96f,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "pressable_card_scale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation / 2 else elevation,
        animationSpec = AppAnimations.fastTween(),
        label = "pressable_card_elevation"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 悬浮卡片 - 带悬浮提升效果
// ───────────────────────────────────────────────────────────

@Composable
fun HoverCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = AppShapes.CardMedium,
    backgroundColor: Color = Color.White,
    baseElevation: Dp = 2.dp,
    hoverElevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isHovered) hoverElevation else baseElevation,
        animationSpec = AppAnimations.normalTween(),
        label = "hover_card_elevation"
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
// 淡入淡出动画容器
// ───────────────────────────────────────────────────────────

@Composable
fun FadeInOutContainer(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = AppAnimations.normalTween()),
        exit = fadeOut(animationSpec = AppAnimations.normalTween())
    ) {
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 滑入滑出动画容器
// ───────────────────────────────────────────────────────────

@Composable
fun SlideInOutContainer(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = AppAnimations.normalTween(),
            initialOffsetY = { -it }
        ) + fadeIn(animationSpec = AppAnimations.normalTween()),
        exit = slideOutVertically(
            animationSpec = AppAnimations.normalTween(),
            targetOffsetY = { -it }
        ) + fadeOut(animationSpec = AppAnimations.normalTween())
    ) {
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 展开收起动画容器
// ───────────────────────────────────────────────────────────

@Composable
fun ExpandCollapseContainer(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = AppAnimations.normalTween(),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = AppAnimations.normalTween()),
        exit = shrinkVertically(
            animationSpec = AppAnimations.normalTween(),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = AppAnimations.normalTween())
    ) {
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 脉冲动画 - 用于强调元素
// ───────────────────────────────────────────────────────────

@Composable
fun PulseBox(
    pulsing: Boolean,
    modifier: Modifier = Modifier,
    pulseScale: Float = 1.05f,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (pulsing) pulseScale else 1f,
        animationSpec = AppAnimations.gentleSpring(),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.scale(animatedScale)
    ) {
        content()
    }
}

// ───────────────────────────────────────────────────────────
// 旋转动画容器
// ───────────────────────────────────────────────────────────

@Composable
fun RotatingBox(
    rotating: Boolean,
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 180f,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedRotation by animateFloatAsState(
        targetValue = if (rotating) rotationDegrees else 0f,
        animationSpec = AppAnimations.normalTween(),
        label = "rotation"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = animatedRotation
        }
    ) {
        content()
    }
}
