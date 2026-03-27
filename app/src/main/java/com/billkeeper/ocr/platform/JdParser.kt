package com.billkeeper.ocr.platform

import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.ocr.OcrResult
import com.billkeeper.ocr.ParsedBillInfo

/**
 * 京东支付解析模板
 */
class JdParser : PlatformParser {

    private val amountPatterns = listOf(
        Regex("[¥￥]\\s*(\\d+\\.\\d{1,2})"),
        Regex("支付[\\s]*([\\d,]+\\.\\d{1,2})"),
        Regex("实付[：:]*\\s*[¥￥]?\\s*([\\d,]+\\.\\d{1,2})")
    )

    private val payeePatterns = listOf(
        Regex("商户[：:]*\\s*(.{2,30})"),
        Regex("店铺[：:]*\\s*(.{2,30})"),
        Regex("向(.{2,30}?)(?:支付|付款)")
    )

    override fun parse(ocrResult: OcrResult): ParsedBillInfo {
        val text = ocrResult.fullText

        val isJdPage = text.contains("京东")
        if (!isJdPage) return ParsedBillInfo(rawText = text)

        var amount = 0.0
        var payee = ""

        for (pattern in amountPatterns) {
            pattern.find(text)?.let { match ->
                try { amount = match.groupValues[1].replace(",", "").toDouble() }
                catch (_: NumberFormatException) {}
            }
            if (amount > 0) break
        }

        for (pattern in payeePatterns) {
            pattern.find(text)?.let { match ->
                payee = match.groupValues[1].trim()
            }
            if (payee.isNotBlank()) break
        }

        var score = 0.3f
        if (text.contains("京东")) score += 0.2f
        if (amount > 0) score += 0.3f
        if (payee.isNotBlank()) score += 0.2f

        return ParsedBillInfo(
            amount = amount,
            payee = payee,
            payerAccount = "京东支付",
            platform = PaymentPlatform.JD,
            confidence = score.coerceIn(0f, 1f),
            rawText = text
        )
    }
}
