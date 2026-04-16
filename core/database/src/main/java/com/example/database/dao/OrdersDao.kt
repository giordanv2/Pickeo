package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.database.model.OrderEntity
import com.example.database.model.OrderItemEntity
import com.example.database.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdersDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAtEpochMillis DESC")
    fun observeOrders(): Flow<List<OrderWithItems>>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun countOrders(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        insertOrders(listOf(order))
        insertOrderItems(items)
    }

    @Query("DELETE FROM order_items")
    suspend fun clearOrderItems()

    @Query("DELETE FROM orders")
    suspend fun clearOrders()
}
