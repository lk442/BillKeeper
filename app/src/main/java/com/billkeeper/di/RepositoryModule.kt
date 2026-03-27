package com.billkeeper.di

import com.billkeeper.data.repository.BillRepository
import com.billkeeper.data.repository.TagRepository
import com.billkeeper.data.repository.WorkbookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBillRepository(billRepository: BillRepository): BillRepository = billRepository

    @Provides
    @Singleton
    fun provideTagRepository(tagRepository: TagRepository): TagRepository = tagRepository

    @Provides
    @Singleton
    fun provideWorkbookRepository(workbookRepository: WorkbookRepository): WorkbookRepository = workbookRepository
}
