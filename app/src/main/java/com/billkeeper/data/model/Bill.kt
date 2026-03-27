package com.billkeeper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账单数据模型
 */
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val amount: Double,
    val payerAccount: String = "",
    val payee: String = "",
    val platform: String = PaymentPlatform.MANUAL.name,
    val tags: String = "",       // JSON数组格式的标签列表
    val remark: String = "",
    val workbookId: Long = 1,
    val source: String = BillSource.MANUAL.name,
    val incomeType: String = IncomeType.EXPENSE.name,  // 收支类型
    val isDeleted: Boolean = false
)
