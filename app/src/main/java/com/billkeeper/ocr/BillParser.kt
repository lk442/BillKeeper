package com.billkeeper.ocr

import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.ocr.platform.*

/**
 * 账单解析结果
 */
data class ParsedBillInfo(
    val amount: Double = 0.0,
    val payee: String = "",
    val payerAccount: String = "",
    val platform: PaymentPlatform = PaymentPlatform.MANUAL,
    val confidence: Float = 0f,
    val rawText: String = ""
)

/**
 * 账单解析器 - 根据平台选择对应解析模板
 */
object BillParser {

    private val parsers = mapOf(
        PaymentPlatform.WECHAT to WeChatParser(),
        PaymentPlatform.ALIPAY to AlipayParser(),
        PaymentPlatform.MEITUAN to MeituanParser(),
        PaymentPlatform.JD to JdParser(),
        PaymentPlatform.TAOBAO to TaobaoParser(),
        PaymentPlatform.BANK_CARD to BankCardParser()
    )

    /**
     * 解析OCR文本中的账单信息
     * @param ocrResult OCR识别结果
     * @param platform 指定支付平台（null则自动检测）
     * @return 解析出的账单信息
     */
    fun parse(ocrResult: OcrResult, platform: PaymentPlatform? = null): ParsedBillInfo {
        val detectedPlatform = platform ?: detectPlatform(ocrResult.fullText)
        val parser = parsers[detectedPlatform] ?: return ParsedBillInfo(rawText = ocrResult.fullText)

        return parser.parse(ocrResult)
    }

    /**
     * 自动检测支付平台
     */
    private fun detectPlatform(text: String): PaymentPlatform {
        return when {
            text.contains("微信支付") || text.contains("微信") -> PaymentPlatform.WECHAT
            text.contains("支付宝") || text.contains("Alipay") -> PaymentPlatform.ALIPAY
            text.contains("美团") || text.contains("美团支付") -> PaymentPlatform.MEITUAN
            text.contains("京东") || text.contains("京东支付") -> PaymentPlatform.JD
            text.contains("淘宝") || text.contains("Taobao") -> PaymentPlatform.TAOBAO
            text.contains("银行卡") || text.contains("储蓄卡") || text.contains("信用卡") -> PaymentPlatform.BANK_CARD
            else -> PaymentPlatform.MANUAL
        }
    }

    /**
     * 从OCR结果中提取关键帧文本（过滤掉无意义的行）
     */
    fun extractRelevantLines(ocrResult: OcrResult): List<OcrLine> {
        return ocrResult.lines.filter { line ->
            val text = line.text.trim()
            text.isNotBlank() &&
                    text.length >= 2 &&
                    line.confidence > 0.5f &&
                    // 过滤掉纯数字或纯符号的行
                    !text.matches(Regex("^[\\d\\s\\-\\+.,]+$"))
        }
    }
}
