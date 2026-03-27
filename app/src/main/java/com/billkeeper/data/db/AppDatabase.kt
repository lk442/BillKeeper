package com.billkeeper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.billkeeper.data.model.Bill
import com.billkeeper.data.model.Tag
import com.billkeeper.data.model.Workbook

@Database(
    entities = [Bill::class, Tag::class, Workbook::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun tagDao(): TagDao
    abstract fun workbookDao(): WorkbookDao

    companion object {
        // 数据库迁移：版本1到版本2，添加incomeType字段
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加incomeType列，默认值为EXPENSE
                database.execSQL(
                    "ALTER TABLE bills ADD COLUMN incomeType TEXT NOT NULL DEFAULT 'EXPENSE'"
                )
            }
        }
    }
}
