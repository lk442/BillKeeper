package com.billkeeper.data.model

/**
 * 分类统计数据
 */
data class CategoryStatistics(
    val category: String,
    val total: Double
)

/**
 * 月度统计数据
 */
data class MonthlyStatistics(
    val month: String,
    val income: Double,
    val expense: Double
)

/**
 * 日统计数据
 */
data class DailyStatistics(
    val dayTimestamp: Long,
    val income: Double,
    val expense: Double
)

/**
 * 综合统计数据
 */
data class StatisticsData(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val categoryStatistics: List<CategoryStatistics>,
    val monthlyStatistics: List<MonthlyStatistics>,
    val dailyStatistics: List<DailyStatistics>
)
