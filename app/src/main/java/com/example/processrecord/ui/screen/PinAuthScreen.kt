package com.example.processrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.processrecord.data.PinManager

private const val PIN_LENGTH = 4

@Composable
private fun PinDots(enteredLength: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(PIN_LENGTH) { index ->
            val filled = index < enteredLength
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

@Composable
private fun NumPad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (key in row) {
                    when {
                        key.isEmpty() -> Spacer(modifier = Modifier.size(72.dp))
                        key == "⌫" -> FilledTonalButton(
                            onClick = onDelete,
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = key, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                        }
                        else -> FilledTonalButton(
                            onClick = { onDigit(key) },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = key, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pin) {
        if (pin.length == PIN_LENGTH) {
            if (pinManager.verifyPin(pin)) {
                onAuthenticated()
            } else {
                errorMessage = "密码错误，请重试"
                pin = ""
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("请输入密码") })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "应用已锁定",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))
                PinDots(enteredLength = pin.length)
                Spacer(modifier = Modifier.height(12.dp))
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            NumPad(
                onDigit = { digit ->
                    if (pin.length < PIN_LENGTH) {
                        pin += digit
                        errorMessage = null
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    errorMessage = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }

    // step 0 = verify existing PIN (only if one is already set)
    // step 1 = enter new PIN
    // step 2 = confirm new PIN
    val startStep = if (pinManager.isPinEnabled) 0 else 1
    var step by remember { mutableStateOf(startStep) }
    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    val title = when (step) {
        0 -> "验证当前密码"
        1 -> if (pinManager.isPinEnabled) "输入新密码" else "设置密码"
        else -> "再次输入新密码"
    }

    LaunchedEffect(pin) {
        if (pin.length == PIN_LENGTH) {
            when (step) {
                0 -> {
                    if (pinManager.verifyPin(pin)) {
                        step = 1
                        pin = ""
                        errorMessage = null
                    } else {
                        errorMessage = "密码错误，请重试"
                        pin = ""
                    }
                }
                1 -> {
                    firstPin = pin
                    step = 2
                    pin = ""
                    errorMessage = null
                }
                2 -> {
                    if (pin == firstPin) {
                        pinManager.setPin(pin)
                        navigateBack()
                    } else {
                        errorMessage = "两次输入不一致，请重新输入"
                        step = 1
                        pin = ""
                        firstPin = ""
                    }
                }
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("关闭密码保护") },
            text = { Text("确认要关闭密码保护吗？") },
            confirmButton = {
                TextButton(onClick = {
                    pinManager.removePin()
                    showRemoveDialog = false
                    navigateBack()
                }) { Text("确认关闭") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))
                PinDots(enteredLength = pin.length)
                Spacer(modifier = Modifier.height(12.dp))
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            NumPad(
                onDigit = { digit ->
                    if (pin.length < PIN_LENGTH) {
                        pin += digit
                        errorMessage = null
                    }
                },
                onDelete = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    errorMessage = null
                }
            )
            if (pinManager.isPinEnabled) {
                TextButton(onClick = { showRemoveDialog = true }) {
                    Text("关闭密码保护", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
