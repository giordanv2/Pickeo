package com.example.database.di

import android.content.Context
import androidx.room.Room
import com.example.database.dao.CartItemDao
import com.example.database.source.CartDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCartDataSource(
        @ApplicationContext context: Context
    ): CartDataSource = Room.databaseBuilder(
        context = context,
        klass = CartDataSource::class.java,
        name = "cart_data_source.db"
    ).build()

    @Provides
    fun provideCartItemDao(dataSource: CartDataSource): CartItemDao = dataSource.cartItemDao()
}

