package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_records",
    foreignKeys = [
        ForeignKey(
            entity = Process::class,
            parentColumns = ["id"],
            childColumns = ["processId"],
            // Keep records even if a process is deleted; only detach the process reference.
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["createTime"]),
        Index(value = ["processId"]),
        Index(value = ["entryGroupId"])
    ]
)
data class WorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val processId: Long?,
    // Snapshot field to keep display name when the linked process is removed/renamed.
    val processName: String,
    val style: String,
    val entryGroupId: String = "",
    // Monetary values are stored in cents.
    val unitPrice: Long,
    val quantity: Long,
    // Monetary values are stored in cents.
    val amount: Long,
    val startTime: Long = 0,
    val endTime: Long = 0,
    val remark: String = "",
    val totalQuantity: Long = 0,
    val serialNumber: String = "",
    val color: String = "",
    // Date bucket timestamp (normalized to local day start).
    val date: Long,
    val createTime: Long = System.currentTimeMillis()
)
