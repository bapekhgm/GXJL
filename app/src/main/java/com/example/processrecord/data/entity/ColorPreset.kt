package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "color_presets",
    indices = [Index(value = ["name"], unique = true)]
)
data class ColorPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hexValue: String,
    val groupId: Long = 0,
    val sortOrder: Int = 0
)
