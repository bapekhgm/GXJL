package com.example.processrecord.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing

// ───────────────────────────────────────────────────────────
// 动画配置 - 统一的动画时长和缓动函数
// ───────────────────────────────────────────────────────────

object AppAnimations {
    // 动画时长
    const val DurationInstant = 50
    const val DurationFast = 150
    const val DurationNormal = 300
    const val DurationSlow = 500
    const val DurationVerySlow = 800

    // 标准缓动函数
    val EaseInOut = FastOutSlowInEasing
    val EaseOut = LinearOutSlowInEasing
    val EaseInOutCubic = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)

    // Tween 动画规格
    fun <T> fastTween(): FiniteAnimationSpec<T> = tween(
        durationMillis = DurationFast,
        easing = EaseInOut
    )

    fun <T> normalTween(): FiniteAnimationSpec<T> = tween(
        durationMillis = DurationNormal,
        easing = EaseInOut
    )

    fun <T> slowTween(): FiniteAnimationSpec<T> = tween(
        durationMillis = DurationSlow,
        easing = EaseInOut
    )

    // Spring 动画规格
    fun <T> gentleSpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> bouncySpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> stiffSpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
}
