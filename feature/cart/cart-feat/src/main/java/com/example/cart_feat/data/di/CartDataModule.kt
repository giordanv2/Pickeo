package com.example.cart_feat.data.di

import com.example.cart_feat.data.local.RoomCartDataSource
import com.example.cart_lib.repository.CartRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CartDataModule {

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        dataSource: RoomCartDataSource
    ): CartRepository
}
