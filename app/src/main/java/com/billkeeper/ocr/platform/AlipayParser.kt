package com.billkeeper.ocr.platform

import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.ocr.OcrResult
import com.billkeeper.ocr.ParsedBillInfo

/**
 * 支付宝账单解析模板
 *
 * 支付宝支付成功页面特征文本示例：
 * - "付款成功"
 * - "¥12.34"
 * - "付款给XXX" / "商家：XXX"
 * - "付款方式：余额/招商银行(1234)"
 */
class AlipayParser : PlatformParser {

    private val amountPatterns = listOf(
        Regex("[¥￥]\\s*(\\d+\\.\\d{1,2})"),
        Regex("付款[\\s]*([\\d,]+\\.\\d{1,2})"),
        Regex("金额[：:]*\\s*[¥￥]?\\s*([\\d,]+\\.\\d{1,2})")
    )

    private val payeePatterns = listOf(
        Regex("付款给(.{2,30})"),
        Regex("收款方[：:]*\\s*(.{2,30})"),
        Regex("商户[名称]*[：:]*\\s*(.{2,30})"),
        Regex("商家[：:]*\\s*(.{2,30})"),
        Regex("(.{2,20})\\s*已成功收款")
    )

    private val accountPatterns = listOf(
        Regex("付款方式[：:]*\\s*(.{2,30})"),
        Regex("(余额|花呗|余额宝|银行卡)")
    )

    override fun parse(ocrResult: OcrResult): ParsedBillInfo {
        val text = ocrResult.fullText

        val isAlipayPage = text.contains("支付宝") || text.contains("付款成功") ||
                           text.contains("Alipay")
        if (!isAlipayPage) return ParsedBillInfo(rawText = text)

        var amount = 0.0
        var payee = ""
        var payerAccount = ""

        for (pattern in amountPatterns) {
            pattern.find(text)?.let { match ->
                try {
                    amount = match.groupValues[1].replace(",", "").toDouble()
                } catch (_: NumberFormatException) {}
            }
            if (amount > 0) break
        }

        for (pattern in payeePatterns) {
            pattern.find(text)?.let { match ->
                payee = match.groupValues[1].trim()
            }
            if (payee.isNotBlank()) break
        }

        for (pattern in accountPatterns) {
            pattern.find(text)?.let { match ->
                payerAccount = match.groupValues[1].trim()
            }
            if (payerAccount.isNotBlank()) break
        }

        if (payerAccount.isBlank() && amount > 0) {
            payerAccount = "支付宝"
        }

        var score = 0.3f
        if (text.contains("付款成功")) score += 0.2f
        if (text.contains("支付宝")) score += 0.15f
        if (amount > 0) score += 0.2f
        if (payee.isNotBlank()) score += 0.15f

        return ParsedBillInfo(
            amount = amount,
            payee = payee,
            payerAccount = payerAccount,
            platform = PaymentPlatform.ALIPAY,
            confidence = score.coerceIn(0f, 1f),
            rawText = text
        )
    }
}
