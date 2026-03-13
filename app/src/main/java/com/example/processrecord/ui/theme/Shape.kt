package com.example.processrecord.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ───────────────────────────────────────────────────────────
// 形状系统 - 统一的圆角规范
// ───────────────────────────────────────────────────────────

object AppShapes {
    // 基础圆角
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Full = RoundedCornerShape(50)

    // 卡片圆角
    val CardSmall = RoundedCornerShape(12.dp)
    val CardMedium = RoundedCornerShape(16.dp)
    val CardLarge = RoundedCornerShape(20.dp)

    // 按钮圆角
    val ButtonSmall = RoundedCornerShape(8.dp)
    val ButtonMedium = RoundedCornerShape(12.dp)
    val ButtonLarge = RoundedCornerShape(16.dp)
    val ButtonPill = RoundedCornerShape(50)

    // 输入框圆角
    val TextField = RoundedCornerShape(12.dp)
    val TextFieldFocused = RoundedCornerShape(16.dp)

    // 对话框圆角
    val Dialog = RoundedCornerShape(24.dp)
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    // 芯片/标签圆角
    val Chip = RoundedCornerShape(8.dp)
    val ChipPill = RoundedCornerShape(50)

    // 特殊形状 - 顶部圆角
    val TopRounded = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val TopRoundedLarge = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    // 特殊形状 - 底部圆角
    val BottomRounded = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    val BottomRoundedLarge = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

    // 不对称圆角 - 用于创意设计
    val AsymmetricSmall = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomEnd = 16.dp,
        bottomStart = 4.dp
    )

    val AsymmetricLarge = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 8.dp,
        bottomEnd = 24.dp,
        bottomStart = 8.dp
    )
}
