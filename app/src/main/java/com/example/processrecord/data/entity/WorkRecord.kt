package com.example.processrecord.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_records",
    foreignKeys = [
        ForeignKey(
            entity = Process::class,
            parentColumns = ["id"],
            childColumns = ["processId"],
            onDelete = ForeignKey.SET_NULL // 工序删除后，记录保留但解除关联
        )
    ]
)
data class WorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val processId: Long?,
    val processName: String, // 快照：防止工序被删后无法显示名称
    val style: String, // 款号
    val unitPrice: Double, // 快照：记录时的单价
    val quantity: Double, // 数量
    val amount: Double, // 总金额
    val startTime: Long = 0, // 开始时间戳
    val endTime: Long = 0, // 结束时间戳
    val remark: String = "", // 备注
    val totalQuantity: Double = 0.0, // 总数量 (选填)
    val serialNumber: String = "", // 序号 (选填)
    val color: String = "", // 颜色 (选填)
    val date: Long, // 归属日期 (YYYY-MM-DD 的 timestamp)
    val createTime: Long = System.currentTimeMillis()
)
