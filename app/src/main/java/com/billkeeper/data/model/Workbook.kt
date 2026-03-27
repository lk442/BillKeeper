package com.billkeeper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账本数据模型
 */
@Entity(tableName = "workbooks")
data class Workbook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "默认账本",
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
