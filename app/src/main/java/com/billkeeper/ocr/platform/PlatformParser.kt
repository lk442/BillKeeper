package com.billkeeper.ocr.platform

import com.billkeeper.ocr.OcrResult
import com.billkeeper.ocr.ParsedBillInfo

/**
 * 平台账单解析器接口
 */
interface PlatformParser {
    fun parse(ocrResult: OcrResult): ParsedBillInfo
}
