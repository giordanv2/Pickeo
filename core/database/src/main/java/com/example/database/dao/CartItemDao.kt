package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.model.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun findById(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteById(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
