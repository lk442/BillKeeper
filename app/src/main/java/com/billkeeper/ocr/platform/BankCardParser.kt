package com.billkeeper.ocr.platform

import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.ocr.OcrResult
import com.billkeeper.ocr.ParsedBillInfo

/**
 * 银行卡支付解析模板
 */
class BankCardParser : PlatformParser {

    private val amountPatterns = listOf(
        Regex("[¥￥]\\s*(\\d+\\.\\d{1,2})"),
        Regex("支付[\\s]*([\\d,]+\\.\\d{1,2})"),
        Regex("转账金额[：:]*\\s*[¥￥]?\\s*([\\d,]+\\.\\d{1,2})"),
        Regex("交易金额[：:]*\\s*[¥￥]?\\s*([\\d,]+\\.\\d{1,2})")
    )

    private val payeePatterns = listOf(
        Regex("收款人[：:]*\\s*(.{2,30})"),
        Regex("收款方[：:]*\\s*(.{2,30})"),
        Regex("向(.{2,30}?)(?:转账|支付)")
    )

    private val accountPatterns = listOf(
        Regex("付款账户[：:]*\\s*(.{2,30})"),
        Regex("([\\*]+\\d{4})"),
        Regex("(储蓄卡|信用卡|借记卡)[\\s]*(\\S+)")
    )

    override fun parse(ocrResult: OcrResult): ParsedBillInfo {
        val text = ocrResult.fullText

        val isBankPage = text.contains("银行") || text.contains("转账") ||
                         text.contains("储蓄卡") || text.contains("信用卡")
        if (!isBankPage) return ParsedBillInfo(rawText = text)

        var amount = 0.0
        var payee = ""
        var payerAccount = ""

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

        for (pattern in accountPatterns) {
            pattern.find(text)?.let { match ->
                payerAccount = match.groupValues[1].trim()
            }
            if (payerAccount.isNotBlank()) break
        }

        var score = 0.3f
        if (amount > 0) score += 0.3f
        if (payee.isNotBlank()) score += 0.2f
        if (payerAccount.isNotBlank()) score += 0.2f

        return ParsedBillInfo(
            amount = amount,
            payee = payee,
            payerAccount = payerAccount.ifBlank { "银行卡" },
            platform = PaymentPlatform.BANK_CARD,
            confidence = score.coerceIn(0f, 1f),
            rawText = text
        )
    }
}
