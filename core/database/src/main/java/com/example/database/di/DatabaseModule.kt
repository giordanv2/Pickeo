package com.example.database.di

import android.content.Context
import androidx.room.Room
import com.example.database.dao.CartItemDao
import com.example.database.source.CartDatabase
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
    fun provideCartDatabase(
        @ApplicationContext context: Context
    ): CartDatabase = Room.databaseBuilder(
        context = context,
        klass = CartDatabase::class.java,
        name = "cart_data_source.db"
    ).build()

    @Provides
    fun provideCartItemDao(database: CartDatabase): CartItemDao = database.cartItemDao()
}
