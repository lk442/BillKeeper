package com.billkeeper.data.repository

import com.billkeeper.data.db.WorkbookDao
import com.billkeeper.data.model.Workbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookRepository @Inject constructor(
    private val workbookDao: WorkbookDao
) {
    fun getAllWorkbooks(): Flow<List<Workbook>> =
        workbookDao.getAllWorkbooks()

    suspend fun getDefaultWorkbook(): Workbook? = withContext(Dispatchers.IO) {
        workbookDao.getDefaultWorkbook()
    }

    suspend fun getWorkbookById(id: Long): Workbook? = withContext(Dispatchers.IO) {
        workbookDao.getWorkbookById(id)
    }

    suspend fun getBillCount(workbookId: Long): Int = withContext(Dispatchers.IO) {
        workbookDao.getBillCount(workbookId)
    }

    suspend fun createWorkbook(name: String): Long = withContext(Dispatchers.IO) {
        val workbook = Workbook(name = name)
        workbookDao.insertWorkbook(workbook)
    }

    suspend fun setDefaultWorkbook(id: Long) = withContext(Dispatchers.IO) {
        workbookDao.clearDefault()
        workbookDao.setDefault(id)
    }

    suspend fun deleteWorkbook(id: Long) = withContext(Dispatchers.IO) {
        workbookDao.deleteWorkbook(id)
    }
}
