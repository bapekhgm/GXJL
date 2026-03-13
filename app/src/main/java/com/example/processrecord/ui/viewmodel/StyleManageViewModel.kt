package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.StyleRepository
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

private val STYLE_SPLIT_REGEX = Regex("[\\n,\\uFF0C\\u3001\\s]+")

internal fun splitStyleNames(text: String): List<String> {
    return text
        .split(STYLE_SPLIT_REGEX)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.getDefault()) }
}

class StyleManageViewModel(
    private val styleRepository: StyleRepository
) : ViewModel() {

    sealed interface AddStyleResult {
        data object EmptyName : AddStyleResult
        data class Duplicate(val name: String) : AddStyleResult
        data class Added(val name: String) : AddStyleResult
    }

    val styleList: StateFlow<List<Style>> =
        styleRepository.getAllStylesStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addStyle(name: String, onResult: (AddStyleResult) -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onResult(AddStyleResult.EmptyName)
            return
        }
        viewModelScope.launch {
            val existing = styleRepository.getStyleByName(trimmed)
            if (existing != null) {
                onResult(AddStyleResult.Duplicate(trimmed))
            } else {
                val insertedId = styleRepository.insertStyle(Style(name = trimmed))
                if (insertedId == -1L) {
                    onResult(AddStyleResult.Duplicate(trimmed))
                } else {
                    onResult(AddStyleResult.Added(trimmed))
                }
            }
        }
    }

    fun deleteStyle(style: Style) {
        viewModelScope.launch {
            styleRepository.deleteStyle(style)
        }
    }

    fun batchAddStyles(text: String, onResult: (Int, Int) -> Unit) {
        val names = splitStyleNames(text)
        if (names.isEmpty()) {
            onResult(0, 0)
            return
        }
        viewModelScope.launch {
            var added = 0
            var skipped = 0
            names.forEach { name ->
                val insertedId = styleRepository.insertStyle(Style(name = name))
                if (insertedId != -1L) {
                    added++
                } else {
                    skipped++
                }
            }
            onResult(added, skipped)
        }
    }
}
