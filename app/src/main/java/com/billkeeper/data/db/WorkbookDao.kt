package com.billkeeper.data.db

import androidx.room.*
import com.billkeeper.data.model.Workbook
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkbookDao {

    @Query("SELECT * FROM workbooks ORDER BY isDefault DESC, createdAt ASC")
    fun getAllWorkbooks(): Flow<List<Workbook>>

    @Query("SELECT * FROM workbooks WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultWorkbook(): Workbook?

    @Query("SELECT * FROM workbooks WHERE id = :id")
    suspend fun getWorkbookById(id: Long): Workbook?

    @Query("SELECT COUNT(*) FROM bills WHERE workbookId = :workbookId AND isDeleted = 0")
    suspend fun getBillCount(workbookId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkbook(workbook: Workbook): Long

    @Query("UPDATE workbooks SET isDefault = 0")
    suspend fun clearDefault()

    @Query("UPDATE workbooks SET isDefault = 1 WHERE id = :id")
    suspend fun setDefault(id: Long)

    @Query("DELETE FROM workbooks WHERE id = :id AND isDefault = 0")
    suspend fun deleteWorkbook(id: Long)
}
