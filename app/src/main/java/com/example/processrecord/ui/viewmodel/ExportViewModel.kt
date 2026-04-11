package com.example.processrecord.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.processrecord.data.WorkRecordRepository
import com.example.processrecord.data.entity.WorkRecordColorItem
import jxl.Workbook
import jxl.write.Label
import jxl.write.Number
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportViewModel(
    private val workRecordRepository: WorkRecordRepository
) : ViewModel() {

    data class ExportSummary(
        val recordCount: Int,
        val totalAmount: Double
    )

    private fun centsToYuan(cents: Long): Double = cents / 100.0

    suspend fun exportRecordsToExcel(
        context: Context,
        uri: Uri,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<ExportSummary> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val records = when {
                    startDate != null && endDate != null ->
                        workRecordRepository.getRecordsByDateRangeStream(startDate, endDate).first()

                    else -> {
                        val allRecords = workRecordRepository.getAllRecordsStream().first()
                        allRecords.filter { record ->
                            val afterStart = startDate?.let { record.date >= it } ?: true
                            val beforeEnd = endDate?.let { record.date <= it } ?: true
                            afterStart && beforeEnd
                        }
                    }
                }

                val recordIds = records.map { it.id }
                val imagesByRecordId = workRecordRepository.getImagesByRecordIds(recordIds)
                val colorItemsByRecordId = if (recordIds.isEmpty()) {
                    emptyMap()
                } else {
                    workRecordRepository.getColorItemsByRecordIdsStream(recordIds)
                        .first()
                        .groupBy { it.workRecordId }
                }

                val totalAmountYuan = records.sumOf { centsToYuan(it.amount) }
                val exportTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val workbook: WritableWorkbook = Workbook.createWorkbook(outputStream)
                    val sheet: WritableSheet = workbook.createSheet("工序记录", 0)

                    writeSummary(
                        sheet = sheet,
                        exportTime = exportTimeFormat.format(Date()),
                        startDate = startDate,
                        endDate = endDate,
                        recordCount = records.size,
                        totalAmount = totalAmountYuan
                    )

                    val headerRow = 5
                    writeHeader(sheet, headerRow)

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    records.forEachIndexed { index, record ->
                        val row = headerRow + 1 + index
                        val images = imagesByRecordId[record.id].orEmpty().joinToString(" | ")
                        val colorItems = colorItemsByRecordId[record.id].orEmpty()
                        val colorDetail = colorItems.joinToString("; ", transform = ::buildExportColorDetail)

                        val unitPriceYuan = centsToYuan(record.unitPrice)
                        val amountYuan = centsToYuan(record.amount)

                        sheet.addCell(Label(0, row, record.id.toString()))
                        sheet.addCell(Label(1, row, formatDate(dateFormat, record.date)))
                        sheet.addCell(Label(2, row, record.style))
                        sheet.addCell(Label(3, row, record.processName))
                        sheet.addCell(Number(4, row, unitPriceYuan))
                        sheet.addCell(Number(5, row, record.quantity.toDouble()))
                        sheet.addCell(Number(6, row, amountYuan))
                        sheet.addCell(Label(7, row, formatDate(dateFormat, record.startTime)))
                        sheet.addCell(Label(8, row, formatDate(dateFormat, record.endTime)))
                        sheet.addCell(Label(9, row, colorDetail.ifEmpty { record.color }))
                        sheet.addCell(Label(10, row, record.serialNumber))
                        sheet.addCell(Number(11, row, record.totalQuantity.toDouble()))
                        sheet.addCell(Label(12, row, record.remark))
                        sheet.addCell(Label(13, row, images))
                        sheet.addCell(Label(14, row, formatDate(dateFormat, record.createTime)))
                    }

                    val totalRow = headerRow + 1 + records.size
                    sheet.addCell(Label(5, totalRow, "合计"))
                    sheet.addCell(Number(6, totalRow, totalAmountYuan))

                    workbook.write()
                    workbook.close()
                } ?: error("无法写入导出文件")

                ExportSummary(recordCount = records.size, totalAmount = totalAmountYuan)
            }
        }
    }

    private fun writeSummary(
        sheet: WritableSheet,
        exportTime: String,
        startDate: Long?,
        endDate: Long?,
        recordCount: Int,
        totalAmount: Double
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val rangeText = when {
            startDate != null && endDate != null ->
                "${formatDate(dateFormat, startDate)} ~ ${formatDate(dateFormat, endDate)}"

            else -> "全部数据"
        }

        sheet.addCell(Label(0, 0, "导出时间"))
        sheet.addCell(Label(1, 0, exportTime))
        sheet.addCell(Label(0, 1, "导出范围"))
        sheet.addCell(Label(1, 1, rangeText))
        sheet.addCell(Label(0, 2, "记录数"))
        sheet.addCell(Number(1, 2, recordCount.toDouble()))
        sheet.addCell(Label(0, 3, "总金额"))
        sheet.addCell(Number(1, 3, totalAmount))
    }

    private fun writeHeader(sheet: WritableSheet, headerRow: Int) {
        val headers = listOf(
            "编号",
            "记录日期",
            "款号",
            "工序",
            "单价",
            "数量",
            "金额",
            "开始时间",
            "结束时间",
            "颜色明细",
            "序号",
            "总数量",
            "备注",
            "图片路径",
            "创建时间"
        )

        headers.forEachIndexed { col, title ->
            sheet.addCell(Label(col, headerRow, title))
            sheet.setColumnView(col, 18)
        }
    }

    private fun formatDate(dateFormat: SimpleDateFormat, timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return dateFormat.format(Date(timestamp))
    }
}

internal fun buildExportColorDetail(item: WorkRecordColorItem): String {
    val deficitText = item.deficit.trim()
    return buildString {
        append(item.colorName)
        if (item.quantity > 0) append("|${item.quantity}")
        if (deficitText.isNotBlank()) {
            append("|欠数$deficitText")
            if (item.isDeficitResolved) {
                append("(已补)")
            }
        }
        if (item.colorCode.isNotBlank()) append("|${item.colorCode}")
    }
}
