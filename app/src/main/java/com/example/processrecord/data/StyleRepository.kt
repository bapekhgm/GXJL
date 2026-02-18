package com.example.processrecord.data

import com.example.processrecord.data.dao.StyleDao
import com.example.processrecord.data.entity.Style
import kotlinx.coroutines.flow.Flow

class StyleRepository(private val styleDao: StyleDao) {
    fun getAllStylesStream(): Flow<List<Style>> = styleDao.getAllStyles()
    suspend fun insertStyle(style: Style) = styleDao.insertStyle(style)
    suspend fun deleteStyle(style: Style) = styleDao.deleteStyle(style)
    suspend fun deleteStyleByName(name: String) = styleDao.deleteStyleByName(name)
}
