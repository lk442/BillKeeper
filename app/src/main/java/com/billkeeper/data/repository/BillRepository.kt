package com.billkeeper.data.repository

import com.billkeeper.data.db.BillDao
import com.billkeeper.data.excel.ExcelManager
import com.billkeeper.data.model.Bill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepository @Inject constructor(
    private val billDao: BillDao,
    private val excelManager: ExcelManager
) {
    fun getAllBills(workbookId: Long = 1): Flow<List<Bill>> =
        billDao.getAllBills(workbookId)

    fun getBillsPaged(workbookId: Long = 1, limit: Int = 20, offset: Int = 0): Flow<List<Bill>> =
        billDao.getBillsPaged(workbookId, limit, offset)

    suspend fun getBillById(id: Long): Bill? = withContext(Dispatchers.IO) {
        billDao.getBillById(id)
    }

    fun getTodayBills(workbookId: Long = 1, startOfDay: Long, endOfDay: Long): Flow<List<Bill>> =
        billDao.getTodayBills(workbookId, startOfDay, endOfDay)

    fun getBillsByDateRange(workbookId: Long = 1, startTime: Long, endTime: Long): Flow<List<Bill>> =
        billDao.getBillsByDateRange(workbookId, startTime, endTime)

    suspend fun getTotalExpense(workbookId: Long = 1, startTime: Long, endTime: Long): Double =
        withContext(Dispatchers.IO) {
            billDao.getTotalExpense(workbookId, startTime, endTime)
        }

    suspend fun getTodayTotal(workbookId: Long = 1, startOfDay: Long, endOfDay: Long): Double =
        withContext(Dispatchers.IO) {
            billDao.getTodayTotal(workbookId, startOfDay, endOfDay)
        }

    fun searchBills(workbookId: Long = 1, keyword: String): Flow<List<Bill>> =
        billDao.searchBills(workbookId, keyword)

    fun getBillsByPlatform(workbookId: Long = 1, platform: String): Flow<List<Bill>> =
        billDao.getBillsByPlatform(workbookId, platform)

    suspend fun insertBill(bill: Bill): Long = withContext(Dispatchers.IO) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: Bill) = withContext(Dispatchers.IO) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(id: Long) = withContext(Dispatchers.IO) {
        billDao.deleteBill(id)
    }

    suspend fun insertBills(bills: List<Bill>) = withContext(Dispatchers.IO) {
        bills.forEach { billDao.insertBill(it) }
    }

    /**
     * 导出账单为Excel文件
     */
    suspend fun exportToExcel(workbookId: Long = 1, workbookName: String = "默认账本"): File =
        withContext(Dispatchers.IO) {
            val allBills = ArrayList<Bill>()
            billDao.getAllBills(workbookId).collect { allBills.addAll(it) }
            excelManager.exportToExcel(allBills, workbookName)
        }

    /**
     * 从Excel导入账单
     */
    suspend fun importFromExcel(file: File, workbookId: Long = 1): Int =
        withContext(Dispatchers.IO) {
            val bills = excelManager.importFromExcel(file)
                .map { it.copy(workbookId = workbookId) }
            bills.forEach { billDao.insertBill(it) }
            bills.size
        }

    fun getExportedFiles(): List<File> =
        excelManager.getExportedFiles()

    /**
     * 获取统计数据
     */
    suspend fun getStatistics(
        workbookId: Long = 1,
        startTime: Long,
        endTime: Long
    ): StatisticsData = withContext(Dispatchers.IO) {
        val totalIncome = billDao.getTotalIncome(workbookId, startTime, endTime)
        val totalExpense = billDao.getTotalExpense(workbookId, startTime, endTime)
        val categoryStatistics = billDao.getCategoryExpenseStatistics(workbookId, startTime, endTime)
        val monthlyStatistics = billDao.getMonthlyStatistics(workbookId, startTime, endTime)
        val dailyStatistics = billDao.getDailyStatistics(workbookId, startTime, endTime)

        StatisticsData(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            categoryStatistics = categoryStatistics,
            monthlyStatistics = monthlyStatistics,
            dailyStatistics = dailyStatistics
        )
    }
}
