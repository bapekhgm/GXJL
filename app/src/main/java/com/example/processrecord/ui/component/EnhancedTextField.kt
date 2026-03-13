package com.example.processrecord.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import com.example.processrecord.ui.theme.AppTextFieldShape
import com.example.processrecord.ui.theme.appOutlinedTextFieldColors

@Composable
fun EnhancedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
                // Ignore focus request failures
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.then(
            if (autoFocus) Modifier.focusRequester(focusRequester) else Modifier
        ),
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = AppTextFieldShape,
        colors = appOutlinedTextFieldColors(),
        interactionSource = interactionSource
    )
}

@Composable
fun SmartNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "0",
    isError: Boolean = false,
    supportingText: String? = null,
    allowDecimal: Boolean = false,
    maxDecimalPlaces: Int = 2,
    autoFocus: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    EnhancedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = if (allowDecimal) {
                val cleaned = input.filter { it.isDigit() || it == '.' }
                val dotCount = cleaned.count { it == '.' }
                if (dotCount <= 1) {
                    val parts = cleaned.split('.')
                    if (parts.size <= 2 && (parts.size == 1 || parts[1].length <= maxDecimalPlaces)) {
                        cleaned
                    } else {
                        value
                    }
                } else {
                    value
                }
            } else {
                val cleaned = input.filter(Char::isDigit)
                if (cleaned.isEmpty() || cleaned.toLongOrNull() != null) {
                    cleaned
                } else {
                    value
                }
            }
            if (filtered != value) {
                onValueChange(filtered)
            }
        },
        label = { Text(label) },
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) {
                androidx.compose.ui.text.input.KeyboardType.Decimal
            } else {
                androidx.compose.ui.text.input.KeyboardType.Number
            },
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        autoFocus = autoFocus,
        modifier = modifier
    )
}
