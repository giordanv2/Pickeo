package com.example.orders_feat.data.di

import com.example.orders_feat.data.local.RoomOrdersDataSource
import com.example.orders_lib.repository.OrdersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrdersDataModule {

    @Binds
    @Singleton
    abstract fun bindOrdersRepository(
        dataSource: RoomOrdersDataSource
    ): OrdersRepository
}
