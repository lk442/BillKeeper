package com.billkeeper.data.model

/**
 * 支付平台枚举
 */
enum class PaymentPlatform(val displayName: String, val packageName: String) {
    WECHAT("微信", "com.tencent.mm"),
    ALIPAY("支付宝", "com.eg.android.AlipayGphone"),
    MEITUAN("美团", "com.sankuai.meituan"),
    JD("京东", "com.jingdong.app.mall"),
    TAOBAO("淘宝", "com.taobao.taobao"),
    BANK_CARD("银行卡", ""),
    MANUAL("手动", "");

    companion object {
        fun fromPackageName(packageName: String): PaymentPlatform =
            entries.firstOrNull { it.packageName == packageName } ?: MANUAL
    }
}

/**
 * 账单来源枚举
 */
enum class BillSource(val displayName: String) {
    AUTO("自动识别"),
    FLOATING_WINDOW("悬浮窗"),
    SCREEN_RECORD("录屏"),
    SCREENSHOT("截图"),
    MANUAL("手动输入")
}
