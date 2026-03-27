package com.billkeeper.data.excel

import android.content.Context
import com.billkeeper.data.model.Bill
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelManager(private val context: Context) {

    companion object {
        private const val EXPORT_DIR = "exports"
        private const val FILE_PREFIX = "账单_"
        private const val FILE_EXTENSION = ".xlsx"

        // Excel表头
        private val HEADERS = arrayOf(
            "序号", "交易时间", "付款金额", "付款账户", "收款方",
            "支付平台", "标签", "备注", "记录来源"
        )

        // 样式配置
        private const val HEADER_BG_COLOR_HEX = "4CAF50"
        private const val HEADER_FONT_COLOR_HEX = "FFFFFF"
        private const val EVEN_ROW_COLOR_HEX = "E8F5E9"
        private const val AMOUNT_FORMAT = "#,##0.00"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    private fun getExportDir(): File {
        val dir = File(context.getExternalFilesDir(null), EXPORT_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * 导出账单为Excel文件
     * @param bills 账单列表
     * @param workbookName 账本名称
     * @return 导出的文件路径
     */
    fun exportToExcel(bills: List<Bill>, workbookName: String = "默认账本"): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(workbookName)

        // 创建样式
        val headerStyle = createHeaderStyle(workbook)
        val amountStyle = createAmountStyle(workbook)
        val dateStyle = createDateStyle(workbook)

        // 写入表头
        val headerRow = sheet.createRow(0)
        HEADERS.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // 写入数据
        bills.forEachIndexed { index, bill ->
            val row = sheet.createRow(index + 1)

            // 序号
            row.createCell(0).setCellValue((index + 1).toDouble())

            // 交易时间
            val dateCell = row.createCell(1)
            dateCell.setCellValue(dateFormat.format(Date(bill.date)))
            dateCell.cellStyle = dateStyle

            // 付款金额
            val amountCell = row.createCell(2)
            amountCell.setCellValue(bill.amount)
            amountCell.cellStyle = amountStyle

            // 付款账户
            row.createCell(3).setCellValue(bill.payerAccount)

            // 收款方
            row.createCell(4).setCellValue(bill.payee)

            // 支付平台
            row.createCell(5).setCellValue(getPlatformDisplayName(bill.platform))

            // 标签
            row.createCell(6).setCellValue(bill.tags)

            // 备注
            row.createCell(7).setCellValue(bill.remark)

            // 记录来源
            row.createCell(8).setCellValue(getSourceDisplayName(bill.source))

            // 偶数行背景色
            if (index % 2 == 1) {
                val evenStyle = workbook.createCellStyle()
                evenStyle.fillForegroundColor = IndexedColors.PALE_GREEN.index
                evenStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
                for (col in HEADERS.indices) {
                    row.getCell(col)?.cellStyle = evenStyle
                }
            }
        }

        // 添加合计行
        if (bills.isNotEmpty()) {
            val totalRow = sheet.createRow(bills.size + 1)
            val totalCell = totalRow.createCell(2)
            totalCell.setCellValue(bills.sumOf { it.amount })
            totalCell.cellStyle = createTotalStyle(workbook)

            val labelCell = totalRow.createCell(0)
            labelCell.setCellValue("合计")
        }

        // 自动调整列宽
        HEADERS.indices.forEach { col ->
            sheet.autoSizeColumn(col)
            // 增加一点额外宽度防止内容被截断
            sheet.setColumnWidth(col, sheet.getColumnWidth(col) + 1000)
        }

        // 保存文件
        val fileName = FILE_PREFIX + fileDateFormat.format(Date()) + FILE_EXTENSION
        val file = File(getExportDir(), fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        return file
    }

    /**
     * 从Excel文件导入账单
     */
    fun importFromExcel(file: File): List<Bill> {
        val bills = mutableListOf<Bill>()
        val workbook = WorkbookFactory.create(FileInputStream(file))
        val sheet = workbook.getSheetAt(0)

        // 跳过表头，从第二行开始
        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex) ?: continue

            try {
                val dateStr = row.getCell(1)?.stringCellValue ?: ""
                val date = dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                val amount = row.getCell(2)?.numericCellValue ?: 0.0
                val payerAccount = row.getCell(3)?.stringCellValue ?: ""
                val payee = row.getCell(4)?.stringCellValue ?: ""
                val platform = row.getCell(5)?.stringCellValue ?: ""
                val tags = row.getCell(6)?.stringCellValue ?: ""
                val remark = row.getCell(7)?.stringCellValue ?: ""
                val source = row.getCell(8)?.stringCellValue ?: ""

                bills.add(
                    Bill(
                        date = date,
                        amount = amount,
                        payerAccount = payerAccount,
                        payee = payee,
                        platform = platform,
                        tags = tags,
                        remark = remark,
                        source = source
                    )
                )
            } catch (_: Exception) {
                // 跳过解析失败的行
                continue
            }
        }

        workbook.close()
        return bills
    }

    /**
     * 获取所有已导出的Excel文件列表
     */
    fun getExportedFiles(): List<File> {
        val dir = getExportDir()
        return dir.listFiles { file ->
            file.extension == "xlsx" && file.name.startsWith(FILE_PREFIX)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.SEA_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER

            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 12
                color = IndexedColors.WHITE.index
            }
            this.font = font

            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun createAmountStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat(AMOUNT_FORMAT)
            alignment = HorizontalAlignment.RIGHT
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun createDateStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun createTotalStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_ORANGE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            dataFormat = workbook.createDataFormat().getFormat(AMOUNT_FORMAT)
            alignment = HorizontalAlignment.RIGHT

            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 12
            }
            this.font = font

            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun getPlatformDisplayName(platformName: String): String {
        return try {
            com.billkeeper.data.model.PaymentPlatform.valueOf(platformName).displayName
        } catch (_: Exception) {
            platformName
        }
    }

    private fun getSourceDisplayName(sourceName: String): String {
        return try {
            com.billkeeper.data.model.BillSource.valueOf(sourceName).displayName
        } catch (_: Exception) {
            sourceName
        }
    }
}
