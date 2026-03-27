package com.billkeeper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Parcelable
import android.view.accessibility.AccessibilityEvent
import com.billkeeper.data.model.PaymentPlatform
import kotlinx.coroutines.*
import java.util.regex.Pattern

/**
 * 无障碍服务 - 监听支付应用通知，自动解析账单信息
 */
class BillAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 监听的支付应用包名
    private val targetPackages = setOf(
        PaymentPlatform.WECHAT.packageName,      // 微信
        PaymentPlatform.ALIPAY.packageName,       // 支付宝
        PaymentPlatform.MEITUAN.packageName,      // 美团
        PaymentPlatform.JD.packageName,           // 京东
        PaymentPlatform.TAOBAO.packageName        // 淘宝
    )

    // 金额匹配正则 - 匹配 ¥12.34 或 -12.34 等格式
    private val amountPatterns = listOf(
        Pattern.compile("[-¥￥]\\s*(\\d+\\.?\\d*)"),
        Pattern.compile("支付.*?(\\d+\\.?\\d{0,2})元?"),
        Pattern.compile("成功.*?(\\d+\\.?\\d{0,2})"),
        Pattern.compile("付款.*?(\\d+\\.?\\d{0,2})"),
        Pattern.compile("收款.*?(\\d+\\.?\\d{0,2})"),
        Pattern.compile("(\\d{1,10}\\.\\d{2})")
    )

    // 收款方匹配正则
    private val payeePatterns = listOf(
        Pattern.compile("向(.{2,30}?)(?:支付|付款|转账)"),
        Pattern.compile("付款给(.{2,30})"),
        Pattern.compile("收款方[：:]?(.{2,30})"),
        Pattern.compile("商户[：:]?(.{2,30})"),
        Pattern.compile("(.{2,20})(?:成功收款|已收款)")
    )

    // 已处理的通知ID集合（避免重复）
    private val processedEvents = mutableSetOf<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_DEFAULT
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in targetPackages) return

        // 避免重复处理
        val eventKey = "${event.source?.hashCode()}:${event.eventTime}"
        if (eventKey in processedEvents) return
        processedEvents.add(eventKey)

        // 清理过期的key
        if (processedEvents.size > 100) {
            processedEvents.clear()
        }

        // 解析通知文本
        val texts = mutableListOf<String>()
        event.text?.forEach { texts.add(it.toString()) }
        val contentText = event.contentDescription?.toString()
        if (!contentText.isNullOrBlank()) texts.add(contentText)

        val fullText = texts.joinToString(" ")
        if (fullText.isBlank()) return

        // 解析账单信息
        scope.launch {
            val parsedBill = parseNotification(fullText, packageName)
            if (parsedBill != null) {
                // 发送广播通知UI层
                sendBillBroadcast(parsedBill)
            }
        }
    }

    override fun onInterrupt() {}

    /**
     * 解析通知文本中的账单信息
     */
    private fun parseNotification(text: String, packageName: String): ParsedBill? {
        val platform = PaymentPlatform.fromPackageName(packageName)

        // 解析金额
        var amount = 0.0
        var amountMatched = false
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                try {
                    val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                    amount = amountStr.toDouble()
                    if (amount > 0) {
                        amountMatched = true
                        break
                    }
                } catch (_: NumberFormatException) {
                    continue
                }
            }
        }
        if (!amountMatched) return null

        // 解析收款方
        var payee = ""
        for (pattern in payeePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                payee = matcher.group(1)?.trim() ?: ""
                if (payee.isNotBlank()) break
            }
        }

        // 付款账户（根据平台推断）
        val payerAccount = when (platform) {
            PaymentPlatform.WECHAT -> "微信支付"
            PaymentPlatform.ALIPAY -> "支付宝"
            PaymentPlatform.MEITUAN -> "美团支付"
            PaymentPlatform.JD -> "京东支付"
            PaymentPlatform.TAOBAO -> "淘宝支付"
            else -> ""
        }

        return ParsedBill(
            amount = amount,
            payee = payee,
            payerAccount = payerAccount,
            platform = platform
        )
    }

    /**
     * 发送解析到的账单信息广播
     */
    private fun sendBillBroadcast(bill: ParsedBill) {
        val intent = Intent(ACTION_BILL_PARSED).apply {
            putExtra(EXTRA_AMOUNT, bill.amount)
            putExtra(EXTRA_PAYEE, bill.payee)
            putExtra(EXTRA_PAYER_ACCOUNT, bill.payerAccount)
            putExtra(EXTRA_PLATFORM, bill.platform.name)
            `package` = packageName
        }
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_BILL_PARSED = "com.billkeeper.ACTION_BILL_PARSED"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_PAYEE = "extra_payee"
        const val EXTRA_PAYER_ACCOUNT = "extra_payer_account"
        const val EXTRA_PLATFORM = "extra_platform"
    }

    data class ParsedBill(
        val amount: Double,
        val payee: String,
        val payerAccount: String,
        val platform: PaymentPlatform
    )
}
