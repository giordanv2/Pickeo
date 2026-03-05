package com.example.database.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.CatalogItemDao
import com.example.database.dao.CartItemDao
import com.example.database.model.CatalogItemEntity
import com.example.database.model.CartItemEntity

@Database(
    entities = [CartItemEntity::class, CatalogItemEntity::class],
    version = 3,
    exportSchema = false
)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDao
    abstract fun catalogItemDao(): CatalogItemDao
}
