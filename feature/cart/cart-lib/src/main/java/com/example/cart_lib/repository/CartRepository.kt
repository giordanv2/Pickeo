package com.example.cart_lib.repository

import com.example.cart_lib.models.CartSummary
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal

interface CartRepository {
    val cart: StateFlow<CartSummary>

    fun addItem(
        productId: String,
        name: String,
        unitPrice: BigDecimal,
        quantity: Int = 1
    )

    fun updateQuantity(productId: String, quantity: Int)

    fun removeItem(productId: String)

    fun clear()
}
