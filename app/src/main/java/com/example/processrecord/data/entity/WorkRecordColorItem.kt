package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_record_color_items",
    foreignKeys = [
        ForeignKey(
            entity = WorkRecord::class,
            parentColumns = ["id"],
            childColumns = ["workRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workRecordId"])]
)
data class WorkRecordColorItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workRecordId: Long,
    val colorName: String,
    val colorHex: String,
    val quantity: Double,
    val sortOrder: Int = 0
)
