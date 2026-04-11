package com.example.catalog_feat.di

import com.example.catalog_feat.data.CatalogRoomDataSourceImpl
import com.example.catalog_lib.data.CatalogRoomDataSource
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
    abstract fun bindCatalogRepository(repository: CatalogRoomDataSourceImpl): CatalogRoomDataSource
}
