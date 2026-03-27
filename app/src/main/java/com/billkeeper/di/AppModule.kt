package com.billkeeper.di

import android.content.Context
import androidx.room.Room
import com.billkeeper.data.db.AppDatabase
import com.billkeeper.data.db.BillDao
import com.billkeeper.data.db.TagDao
import com.billkeeper.data.db.WorkbookDao
import com.billkeeper.data.excel.ExcelManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "billkeeper.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBillDao(database: AppDatabase): BillDao = database.billDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    fun provideWorkbookDao(database: AppDatabase): WorkbookDao = database.workbookDao()

    @Provides
    @Singleton
    fun provideExcelManager(@ApplicationContext context: Context): ExcelManager {
        return ExcelManager(context)
    }
}
