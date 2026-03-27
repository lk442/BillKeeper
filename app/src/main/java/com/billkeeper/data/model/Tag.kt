package com.billkeeper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 标签数据模型
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String = "#66BB6A",
    val usageCount: Int = 0,
    val workbookId: Long = 1
)
