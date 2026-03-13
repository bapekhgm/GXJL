package com.example.processrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.processrecord.R

private val PROCESS_SUFFIX_REGEX = Regex("""\*(\d+)$""")
private val PROCESS_QUICK_SUFFIXES = listOf(1, 2, 3, 4)

internal fun applyProcessSuffix(name: String, suffix: String): String {
    val base = name.replace(PROCESS_SUFFIX_REGEX, "").trimEnd()
    return if (base.isEmpty()) suffix else "$base$suffix"
}

internal fun clearProcessSuffix(name: String): String =
    name.replace(PROCESS_SUFFIX_REGEX, "").trimEnd()

@Composable
fun ProcessSuffixSelector(name: String, onNameChange: (String) -> Unit) {
    val matchResult = PROCESS_SUFFIX_REGEX.find(name)
    val currentNum = matchResult?.groupValues?.getOrNull(1)?.toIntOrNull()
    var customInput by remember(name) {
        mutableStateOf(
            if (currentNum != null && currentNum !in PROCESS_QUICK_SUFFIXES) {
                currentNum.toString()
            } else {
                ""
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PROCESS_QUICK_SUFFIXES.forEach { num ->
            FilterChip(
                selected = currentNum == num,
                onClick = {
                    val newName = if (currentNum == num) {
                        clearProcessSuffix(name)
                    } else {
                        applyProcessSuffix(name, "*$num")
                    }
                    customInput = ""
                    onNameChange(newName)
                },
                label = { Text("*$num") }
            )
        }

        Text(
            text = "*",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = customInput,
            onValueChange = { value ->
                val digits = value.filter { it.isDigit() }.take(4)
                customInput = digits
                if (digits.isNotEmpty()) {
                    onNameChange(applyProcessSuffix(name, "*$digits"))
                } else if (currentNum != null && currentNum !in PROCESS_QUICK_SUFFIXES) {
                    onNameChange(clearProcessSuffix(name))
                }
            },
            placeholder = { Text(stringResource(R.string.process_suffix_custom_placeholder)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(64.dp)
        )
    }
}
