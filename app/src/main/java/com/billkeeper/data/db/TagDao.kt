package com.billkeeper.data.db

import androidx.room.*
import com.billkeeper.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE workbookId = :workbookId ORDER BY usageCount DESC")
    fun getAllTags(workbookId: Long = 1): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Long): Tag?

    @Query("SELECT * FROM tags WHERE name = :name AND workbookId = :workbookId")
    suspend fun getTagByName(name: String, workbookId: Long = 1): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE name = :name AND workbookId = :workbookId")
    suspend fun incrementUsageCount(name: String, workbookId: Long = 1)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)
}
