package com.example.cart_lib.repository

import com.example.cart_lib.models.CartSummary
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CartRepository {
    fun observeCart(): Flow<CartSummary>

    suspend fun addItem(
        productId: String,
        name: String,
        unitPrice: BigDecimal,
        quantity: Int = 1
    )

    suspend fun updateQuantity(productId: String, quantity: Int)

    suspend fun removeItem(productId: String)

    suspend fun clear()
}
