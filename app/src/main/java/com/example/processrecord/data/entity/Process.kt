package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processes")
data class Process(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val defaultPrice: Double,
    val unit: String,
    val isActive: Boolean = true
)
