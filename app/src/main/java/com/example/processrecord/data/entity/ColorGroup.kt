package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "color_groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class ColorGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)
