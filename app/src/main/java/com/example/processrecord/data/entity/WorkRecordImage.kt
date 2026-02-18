package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_record_images",
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
data class WorkRecordImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workRecordId: Long,
    val imagePath: String,
    val createTime: Long = System.currentTimeMillis()
)
