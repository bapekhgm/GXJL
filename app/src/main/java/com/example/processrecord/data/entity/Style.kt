package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "styles")
data class Style(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
