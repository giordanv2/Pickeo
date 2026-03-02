package com.example.database.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.CartItemDao
import com.example.database.model.CartItemEntity

@Database(
    entities = [CartItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDao
}
