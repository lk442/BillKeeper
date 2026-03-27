package com.billkeeper.data.db

import androidx.room.*
import com.billkeeper.data.model.Bill
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND workbookId = :workbookId ORDER BY date DESC")
    fun getAllBills(workbookId: Long = 1): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND workbookId = :workbookId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getBillsPaged(workbookId: Long = 1, limit: Int = 20, offset: Int = 0): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND id = :id")
    suspend fun getBillById(id: Long): Bill?

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND date BETWEEN :startOfDay AND :endOfDay AND workbookId = :workbookId ORDER BY date DESC")
    fun getTodayBills(workbookId: Long = 1, startOfDay: Long, endOfDay: Long): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND date BETWEEN :startTime AND :endTime AND workbookId = :workbookId ORDER BY date DESC")
    fun getBillsByDateRange(workbookId: Long = 1, startTime: Long, endTime: Long): Flow<List<Bill>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bills WHERE isDeleted = 0 AND workbookId = :workbookId AND date BETWEEN :startTime AND :endTime")
    suspend fun getTotalExpense(workbookId: Long = 1, startTime: Long, endTime: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bills WHERE isDeleted = 0 AND workbookId = :workbookId AND date BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTodayTotal(workbookId: Long = 1, startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND workbookId = :workbookId AND (payee LIKE '%' || :keyword || '%' OR remark LIKE '%' || :keyword || '%' OR tags LIKE '%' || :keyword || '%') ORDER BY date DESC")
    fun searchBills(workbookId: Long = 1, keyword: String): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE isDeleted = 0 AND workbookId = :workbookId AND platform = :platform ORDER BY date DESC")
    fun getBillsByPlatform(workbookId: Long = 1, platform: String): Flow<List<Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("UPDATE bills SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteBill(id: Long)

    @Query("DELETE FROM bills WHERE workbookId = :workbookId")
    suspend fun deleteAllBills(workbookId: Long = 1)

    // ============ 统计查询方法 ============

    // 按分类聚合查询（支出）
    @Query("""
        SELECT payee as category, SUM(amount) as total
        FROM bills
        WHERE isDeleted = 0 
        AND workbookId = :workbookId
        AND incomeType = 'EXPENSE'
        AND date BETWEEN :startTime AND :endTime
        GROUP BY payee
        ORDER BY total DESC
    """)
    suspend fun getCategoryExpenseStatistics(workbookId: Long, startTime: Long, endTime: Long): List<CategoryStatistics>

    // 按月聚合查询
    @Query("""
        SELECT strftime('%Y-%m', date/1000, 'unixepoch') as month, 
               SUM(CASE WHEN incomeType = 'INCOME' THEN amount ELSE 0 END) as income,
               SUM(CASE WHEN incomeType = 'EXPENSE' THEN amount ELSE 0 END) as expense
        FROM bills
        WHERE isDeleted = 0 
        AND workbookId = :workbookId
        AND date BETWEEN :startTime AND :endTime
        GROUP BY month
        ORDER BY month
    """)
    suspend fun getMonthlyStatistics(workbookId: Long, startTime: Long, endTime: Long): List<MonthlyStatistics>

    // 按日聚合查询（用于趋势图）
    @Query("""
        SELECT date/1000 as dayTimestamp,
               SUM(CASE WHEN incomeType = 'INCOME' THEN amount ELSE 0 END) as income,
               SUM(CASE WHEN incomeType = 'EXPENSE' THEN amount ELSE 0 END) as expense
        FROM bills
        WHERE isDeleted = 0 
        AND workbookId = :workbookId
        AND date BETWEEN :startTime AND :endTime
        GROUP BY date/1000
        ORDER BY date
    """)
    suspend fun getDailyStatistics(workbookId: Long, startTime: Long, endTime: Long): List<DailyStatistics>

    // 获取总收入
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM bills 
        WHERE isDeleted = 0 
        AND workbookId = :workbookId 
        AND incomeType = 'INCOME'
        AND date BETWEEN :startTime AND :endTime
    """)
    suspend fun getTotalIncome(workbookId: Long, startTime: Long, endTime: Long): Double

    // 获取总支出
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM bills 
        WHERE isDeleted = 0 
        AND workbookId = :workbookId 
        AND incomeType = 'EXPENSE'
        AND date BETWEEN :startTime AND :endTime
    """)
    suspend fun getTotalExpense(workbookId: Long, startTime: Long, endTime: Long): Double
}
