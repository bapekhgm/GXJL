package com.example.processrecord.ui.viewmodel

import com.example.processrecord.data.entity.WorkRecord
import com.example.processrecord.data.entity.Process

// Process extensions
fun Process.toProcessDetails(): ProcessDetails = ProcessDetails(
    id = id,
    name = name,
    defaultPrice = defaultPrice.toString(),
    unit = unit,
    isActive = isActive
)

fun ProcessDetails.toProcess(): Process = Process(
    id = id,
    name = name,
    defaultPrice = defaultPrice.toDoubleOrNull() ?: 0.0,
    unit = unit,
    isActive = isActive
)

// WorkRecord extensions
fun WorkRecord.toWorkRecordDetails(): WorkRecordDetails = WorkRecordDetails(
    id = id,
    processId = processId,
    processName = processName,
    style = style,
    unitPrice = unitPrice.toString(),
    quantity = quantity.toString(),
    amount = amount.toString(),
    startTime = startTime,
    endTime = endTime,
    remark = remark,
    totalQuantity = totalQuantity.toString(),
    serialNumber = serialNumber,
    color = color,
    date = date
)

fun WorkRecordDetails.toWorkRecord(): WorkRecord = WorkRecord(
    id = id,
    processId = processId,
    processName = processName,
    style = style,
    unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toDoubleOrNull() ?: 0.0,
    amount = (quantity.toDoubleOrNull() ?: 0.0) * (unitPrice.toDoubleOrNull() ?: 0.0),
    startTime = startTime,
    endTime = endTime,
    remark = remark,
    totalQuantity = totalQuantity.toDoubleOrNull() ?: 0.0,
    serialNumber = serialNumber,
    color = color,
    date = date
)
