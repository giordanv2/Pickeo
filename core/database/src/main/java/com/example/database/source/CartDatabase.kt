package com.example.database.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.CatalogItemDao
import com.example.database.dao.CartItemDao
import com.example.database.dao.OrdersDao
import com.example.database.model.CatalogItemEntity
import com.example.database.model.CartItemEntity
import com.example.database.model.OrderEntity
import com.example.database.model.OrderItemEntity

@Database(
    entities = [CartItemEntity::class, CatalogItemEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 5,
    exportSchema = false
)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDao
    abstract fun catalogItemDao(): CatalogItemDao
    abstract fun ordersDao(): OrdersDao
}
