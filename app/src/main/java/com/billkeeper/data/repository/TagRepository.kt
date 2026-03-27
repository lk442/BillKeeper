package com.billkeeper.data.repository

import com.billkeeper.data.db.TagDao
import com.billkeeper.data.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    fun getAllTags(workbookId: Long = 1): Flow<List<Tag>> =
        tagDao.getAllTags(workbookId)

    suspend fun getTagById(id: Long): Tag? = withContext(Dispatchers.IO) {
        tagDao.getTagById(id)
    }

    suspend fun getOrCreateTag(name: String, workbookId: Long = 1): Tag =
        withContext(Dispatchers.IO) {
            tagDao.getTagByName(name, workbookId) ?: let {
                val tag = Tag(name = name, workbookId = workbookId)
                val id = tagDao.insertTag(tag)
                tag.copy(id = id)
            }
        }

    suspend fun incrementUsageCount(name: String, workbookId: Long = 1) =
        withContext(Dispatchers.IO) {
            tagDao.incrementUsageCount(name, workbookId)
        }

    suspend fun updateTag(tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.updateTag(tag)
    }

    suspend fun deleteTag(tag: Tag) = withContext(Dispatchers.IO) {
        tagDao.deleteTag(tag)
    }
}
