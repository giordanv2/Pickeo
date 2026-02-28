package com.example.cart_lib.di

import com.example.cart_lib.repository.CartRepository
import com.example.cart_lib.repository.CartRoomRepository
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
        repository: CartRoomRepository
    ): CartRepository
}
