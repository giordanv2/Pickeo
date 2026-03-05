package com.example.catalog_feat.di

import com.example.catalog_lib.data.CatalogRepository
import com.example.catalog_feat.data.CatalogRoomDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogDataModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(repository: CatalogRoomDataSource): CatalogRepository
}
