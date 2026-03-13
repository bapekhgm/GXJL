package com.example.processrecord.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ───────────────────────────────────────────────────────────
// 晨光青春 - 字体排版系统
// 风格：简约时尚、现代清新
// ───────────────────────────────────────────────────────────

// ───────────────────────────────────────────────────────────
// 字体家族
// ───────────────────────────────────────────────────────────

// 使用系统默认字体，保持简约现代感
private val DisplayFontFamily = FontFamily.Default
private val TitleFontFamily = FontFamily.Default
private val BodyFontFamily = FontFamily.Default

// ───────────────────────────────────────────────────────────
// 字体排版系统 - 简约时尚
// ───────────────────────────────────────────────────────────

val Typography = Typography(
    // ───────────────────────────────────────────────────────
    // 显示字体 - 用于大型标题、数字展示
    // ───────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // ───────────────────────────────────────────────────────
    // 标题字体 - 用于页面标题、区块标题
    // ───────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // ───────────────────────────────────────────────────────
    // 标题字体 - 用于组件标题、卡片标题
    // ───────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ───────────────────────────────────────────────────────
    // 正文字体 - 用于主要内容文本
    // ───────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ───────────────────────────────────────────────────────
    // 标签字体 - 用于按钮、标签、导航项
    // ───────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ───────────────────────────────────────────────────────────
// 特殊用途字体样式
// ───────────────────────────────────────────────────────────

// 金额数字 - 用于显示金额 (简约清晰)
val AmountTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
)

// 大金额数字 - 用于突出显示
val LargeAmountTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Black,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.5).sp
)

// 小金额数字 - 用于紧凑显示
val SmallAmountTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// 卡片标题 - 用于卡片内的主要标题
val CardTitleTextStyle = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// 卡片副标题 - 用于卡片内的次要信息
val CardSubtitleTextStyle = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
)

// 款号样式 - 用于款号显示
val StyleNumberTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

// 工序名称样式
val ProcessNameTextStyle = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.15.sp
)

// 标签芯片样式
val ChipTextStyle = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

// 提示文字样式
val HintTextStyle = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
)

// 时间戳样式
val TimestampTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

// ───────────────────────────────────────────────────────────
// 青春活力专用字体样式
// ───────────────────────────────────────────────────────────

// 活力标题 - 用于强调青春感
val VividTitleStyle = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Black,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.5).sp
)

// 活力副标题
val VividSubtitleStyle = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// 活力标签 - 用于按钮、标签
val VividLabelStyle = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.5.sp
)

// 统计数字 - 用于数据展示
val StatNumberStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Black,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-1).sp
)

// 小统计数字
val StatNumberSmallStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.5).sp
)