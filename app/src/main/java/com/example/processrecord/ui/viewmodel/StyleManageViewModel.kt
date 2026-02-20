package com.example.processrecord.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.processrecord.data.StyleRepository
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StyleManageViewModel(
    private val styleRepository: StyleRepository
) : ViewModel() {

    val styleList: StateFlow<List<Style>> =
        styleRepository.getAllStylesStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addStyle(name: String, onResult: (Boolean, String) -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onResult(false, "款号不能为空")
            return
        }
        viewModelScope.launch {
            val existing = styleRepository.getStyleByName(trimmed)
            if (existing != null) {
                onResult(false, "款号「$trimmed」已存在")
            } else {
                styleRepository.insertStyle(Style(name = trimmed))
                onResult(true, "已添加款号「$trimmed」")
            }
        }
    }

    fun deleteStyle(style: Style) {
        viewModelScope.launch {
            styleRepository.deleteStyle(style)
        }
    }

    fun batchAddStyles(text: String, onResult: (Int, Int) -> Unit) {
        val names = text.split("\n", "，", ",", " ", "、")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (names.isEmpty()) {
            onResult(0, 0)
            return
        }
        viewModelScope.launch {
            var added = 0
            var skipped = 0
            names.forEach { name ->
                val existing = styleRepository.getStyleByName(name)
                if (existing == null) {
                    styleRepository.insertStyle(Style(name = name))
                    added++
                } else {
                    skipped++
                }
            }
            onResult(added, skipped)
        }
    }
}