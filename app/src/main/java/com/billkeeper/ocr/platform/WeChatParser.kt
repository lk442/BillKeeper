package com.billkeeper.ocr.platform

import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.ocr.OcrResult
import com.billkeeper.ocr.ParsedBillInfo

/**
 * 微信账单解析模板
 *
 * 微信支付成功页面特征文本示例：
 * - "支付成功"
 * - "¥12.34"
 * - "向XXX付款" / "收款方：XXX"
 * - "付款方式：零钱/招商银行(1234)"
 */
class WeChatParser : PlatformParser {

    // 金额模式
    private val amountPatterns = listOf(
        Regex("[¥￥]\\s*(\\d+\\.\\d{1,2})"),
        Regex("支付[\\s]*([\\d,]+\\.\\d{1,2})"),
        Regex("金额[：:]*\\s*[¥￥]?\\s*([\\d,]+\\.\\d{1,2})")
    )

    // 收款方模式
    private val payeePatterns = listOf(
        Regex("向(.{2,30}?)(?:支付|付款|转账)"),
        Regex("收款方[：:]*\\s*(.{2,30})"),
        Regex("商户[：:]*\\s*(.{2,30})"),
        Regex("(.{2,20})\\s*已成功收款")
    )

    // 付款账户模式
    private val accountPatterns = listOf(
        Regex("付款方式[：:]*\\s*(.{2,30})"),
        Regex("(.+?(?:零钱|储蓄卡|信用卡|银行卡).+)")
    )

    override fun parse(ocrResult: OcrResult): ParsedBillInfo {
        val text = ocrResult.fullText

        // 验证是否为微信支付页面
        val isWechatPage = text.contains("微信") || text.contains("支付成功")
        if (!isWechatPage) return ParsedBillInfo(rawText = text)

        var amount = 0.0
        var payee = ""
        var payerAccount = ""

        // 解析金额
        for (pattern in amountPatterns) {
            pattern.find(text)?.let { match ->
                try {
                    amount = match.groupValues[1].replace(",", "").toDouble()
                } catch (_: NumberFormatException) {}
            }
            if (amount > 0) break
        }

        // 解析收款方
        for (pattern in payeePatterns) {
            pattern.find(text)?.let { match ->
                payee = match.groupValues[1].trim()
            }
            if (payee.isNotBlank()) break
        }

        // 解析付款账户
        for (pattern in accountPatterns) {
            pattern.find(text)?.let { match ->
                payerAccount = match.groupValues[1].trim()
            }
            if (payerAccount.isNotBlank()) break
        }

        // 如果没有解析到账户，给默认值
        if (payerAccount.isBlank() && amount > 0) {
            payerAccount = "微信支付"
        }

        val confidence = calculateConfidence(amount, payee, text)

        return ParsedBillInfo(
            amount = amount,
            payee = payee,
            payerAccount = payerAccount,
            platform = PaymentPlatform.WECHAT,
            confidence = confidence,
            rawText = text
        )
    }

    private fun calculateConfidence(amount: Double, payee: String, text: String): Float {
        var score = 0.3f
        if (text.contains("支付成功")) score += 0.2f
        if (text.contains("微信支付")) score += 0.15f
        if (amount > 0) score += 0.2f
        if (payee.isNotBlank()) score += 0.15f
        return score.coerceIn(0f, 1f)
    }
}
