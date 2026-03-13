package com.example.processrecord.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ───────────────────────────────────────────────────────────
// 清爽深蓝 - 颜色方案定义
// ───────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceTint = SurfaceTintLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceTint = SurfaceTintDark
)

// ───────────────────────────────────────────────────────────
// 渐变定义 - 青春青韵渐变
// ───────────────────────────────────────────────────────────

data class AppGradients(
    // 主渐变 - 青色到薄荷 (清新活力)
    val primary: Brush = Brush.horizontalGradient(
        colors = listOf(GradientPrimaryStart, GradientPrimaryMiddle, GradientPrimaryEnd)
    ),
    // 垂直主渐变
    val primaryVertical: Brush = Brush.verticalGradient(
        colors = listOf(GradientPrimaryStart, GradientPrimaryMiddle, GradientPrimaryEnd)
    ),
    // 对角线主渐变
    val primaryDiagonal: Brush = Brush.linearGradient(
        colors = listOf(GradientPrimaryStart, GradientPrimaryMiddle, GradientPrimaryEnd)
    ),
    // 成功渐变 - 薄荷到翠绿 (清新成长)
    val success: Brush = Brush.horizontalGradient(
        colors = listOf(GradientSuccessStart, GradientSuccessEnd)
    ),
    // 收入渐变 - 柠檬黄到金黄 (收获喜悦)
    val income: Brush = Brush.horizontalGradient(
        colors = listOf(GradientIncomeStart, GradientIncomeEnd)
    ),
    // 活力渐变 - 青到蓝 (自由畅快)
    val romance: Brush = Brush.horizontalGradient(
        colors = listOf(GradientRomanceStart, GradientRomanceEnd)
    ),
    // 海洋渐变 - 青到深蓝 (深邃海洋)
    val ocean: Brush = Brush.horizontalGradient(
        colors = listOf(GradientOceanStart, GradientOceanEnd)
    ),
    // 暗色主题主渐变 - 霓虹青效果
    val primaryDark: Brush = Brush.horizontalGradient(
        colors = listOf(GradientPrimaryDarkStart, GradientPrimaryDarkEnd)
    ),
    // ───────────────────────────────────────────────
    // 高级渐变 - 青春青韵精美效果
    // ───────────────────────────────────────────────
    // 晨光渐变 - 青黄绿 (清晨湖面)
    val aurora: Brush = Brush.linearGradient(
        colors = listOf(AuroraStart, AuroraMiddle, AuroraEnd)
    ),
    val auroraHorizontal: Brush = Brush.horizontalGradient(
        colors = listOf(AuroraStart, AuroraMiddle, AuroraEnd)
    ),
    // 日落渐变 - 青粉橙 (傍晚湖光)
    val sunset: Brush = Brush.linearGradient(
        colors = listOf(SunsetStart, SunsetMiddle, SunsetEnd)
    ),
    // 森林渐变 - 青绿蓝 (清新自然)
    val forest: Brush = Brush.linearGradient(
        colors = listOf(ForestStart, ForestMiddle, ForestEnd)
    ),
    // 薰衣草渐变 - 青紫 (浪漫青春)
    val lavender: Brush = Brush.linearGradient(
        colors = listOf(LavenderStart, LavenderMiddle, LavenderEnd)
    ),
    // 午夜渐变 - 深青蓝 (暗色主题)
    val midnight: Brush = Brush.verticalGradient(
        colors = listOf(MidnightStart, MidnightMiddle, MidnightEnd)
    ),
    // 珊瑚渐变 - 青到浅青 (清新活力)
    val coral: Brush = Brush.linearGradient(
        colors = listOf(CoralStart, CoralMiddle, CoralEnd)
    ),
    // 天际渐变 - 青白 (清新明亮)
    val sky: Brush = Brush.linearGradient(
        colors = listOf(SkyStart, SkyMiddle, SkyEnd)
    ),
    // 薄荷渐变 - 绿青白 (清新薄荷)
    val mint: Brush = Brush.linearGradient(
        colors = listOf(MintStart, MintMiddle, MintEnd)
    ),
    // 霓虹渐变 - 暗色主题强调
    val neon: Brush = Brush.horizontalGradient(
        colors = listOf(NeonPink, NeonCyan)
    ),
    // 霓虹薄荷渐变
    val neonPurpleCyan: Brush = Brush.horizontalGradient(
        colors = listOf(NeonPurple, NeonCyan)
    ),
    // 深海渐变 - 暗色主题背景
    val deepSea: Brush = Brush.verticalGradient(
        colors = listOf(DeepSeaStart, DeepSeaMiddle, DeepSeaEnd)
    ),
    // 活力渐变 - 多彩活力
    val vivid: Brush = Brush.horizontalGradient(
        colors = listOf(VividCoral, VividOrange, VividYellow)
    ),
    // 清新渐变 - 青到天蓝
    val fresh: Brush = Brush.horizontalGradient(
        colors = listOf(VividMint, VividSky)
    )
)

private val LocalGradients = staticCompositionLocalOf { AppGradients() }

// ───────────────────────────────────────────────────────────
// 动画配置
// ───────────────────────────────────────────────────────────

data class AnimationConfig(
    // 卡片动画
    val cardEnterDuration: Int = 300,
    val cardExitDuration: Int = 200,
    // 列表项动画
    val listItemDuration: Int = 200,
    // 按钮动画
    val buttonPressDuration: Int = 100,
    // 页面转场
    val pageTransitionDuration: Int = 300,
    // 涟漪效果
    val rippleDuration: Int = 400,
    // 淡入淡出
    val fadeInDuration: Int = 200,
    val fadeOutDuration: Int = 150,
    // 缩放动画
    val scaleDuration: Int = 150,
    // 滑动动画
    val slideDuration: Int = 250,
    // 弹跳动画
    val bounceDuration: Int = 400
)

private val LocalAnimationConfig = staticCompositionLocalOf { AnimationConfig() }

// ───────────────────────────────────────────────────────────
// 形状配置 - 圆润现代
// ───────────────────────────────────────────────────────────

data class ShapeConfig(
    // 小圆角 - 用于标签、小按钮
    val small: RoundedCornerShape = RoundedCornerShape(12.dp),
    // 中等圆角 - 用于输入框、小卡片
    val medium: RoundedCornerShape = RoundedCornerShape(20.dp),
    // 大圆角 - 用于卡片
    val large: RoundedCornerShape = RoundedCornerShape(28.dp),
    // 超大圆角 - 用于主要卡片、模态框
    val extraLarge: RoundedCornerShape = RoundedCornerShape(36.dp),
    // 圆形 - 使用50%圆角
    val circle: RoundedCornerShape = RoundedCornerShape(50),
    // 底部圆角 - 用于底部弹窗
    val bottomSheet: RoundedCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    // 胶囊形状 - 用于按钮、标签
    val capsule: RoundedCornerShape = RoundedCornerShape(50)
)

private val LocalShapeConfig = staticCompositionLocalOf { ShapeConfig() }

// ───────────────────────────────────────────────────────────
// 主题组合
// ───────────────────────────────────────────────────────────

@Composable
fun ProcessRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalGradients provides AppGradients(),
        LocalAnimationConfig provides AnimationConfig(),
        LocalShapeConfig provides ShapeConfig()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = Shapes(
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(20.dp),
                large = RoundedCornerShape(28.dp)
            ),
            typography = Typography,
            content = content
        )
    }
}

// ───────────────────────────────────────────────────────────
// 便捷访问器
// ───────────────────────────────────────────────────────────

@Composable
fun appGradients() = LocalGradients.current

@Composable
fun animationConfig() = LocalAnimationConfig.current

@Composable
fun shapeConfig() = LocalShapeConfig.current

// ───────────────────────────────────────────────────────────
// 条件颜色
// ───────────────────────────────────────────────────────────

@Composable
fun conditionalColor(light: Color, dark: Color): Color {
    return if (isSystemInDarkTheme()) dark else light
}

@Composable
fun glassBackground(): Color {
    return if (isSystemInDarkTheme()) GlassBackgroundDark else GlassBackgroundLight
}

@Composable
fun glassBorder(): Color {
    return if (isSystemInDarkTheme()) GlassBorderDark else GlassBorderLight
}

@Composable
fun shadowColor(): Color {
    return if (isSystemInDarkTheme()) ShadowDark else ShadowLight
}

// ───────────────────────────────────────────────────────────
// 青春青韵专用颜色访问器
// ───────────────────────────────────────────────────────────

@Composable
fun vividGradient(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.horizontalGradient(colors = listOf(NeonPink, NeonCyan))
    } else {
        Brush.horizontalGradient(colors = listOf(VividCoral, VividMint, VividYellow))
    }
}

@Composable
fun freshGradient(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.horizontalGradient(colors = listOf(NeonCyan, NeonPurple))
    } else {
        Brush.horizontalGradient(colors = listOf(VividMint, VividSky))
    }
}