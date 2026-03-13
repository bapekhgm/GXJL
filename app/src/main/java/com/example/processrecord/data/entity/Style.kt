package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "styles",
    indices = [Index(value = ["name"], unique = true)]
)
data class Style(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
