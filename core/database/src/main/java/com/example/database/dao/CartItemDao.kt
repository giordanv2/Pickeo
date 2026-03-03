package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.model.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun findById(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(item: CartItemEntity): Long

    @Query(
        """
        UPDATE cart_items
        SET name = :name, unitPrice = :unitPrice, quantity = :quantity
        WHERE productId = :productId
        """
    )
    suspend fun updateItem(
        productId: String,
        name: String,
        unitPrice: String,
        quantity: Int
    )

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteById(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
