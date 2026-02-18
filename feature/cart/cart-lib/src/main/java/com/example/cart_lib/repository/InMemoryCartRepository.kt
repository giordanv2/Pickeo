package com.example.cart_lib.repository

import com.example.cart_lib.models.CartItem
import com.example.cart_lib.models.CartSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal

class InMemoryCartRepository : CartRepository {

    private val _cart = MutableStateFlow(CartSummary())
    override val cart: StateFlow<CartSummary> = _cart.asStateFlow()

    override fun addItem(
        productId: String,
        name: String,
        unitPrice: BigDecimal,
        quantity: Int
    ) {
        if (quantity <= 0) return
        _cart.update { summary ->
            val existing = summary.items.firstOrNull { it.productId == productId }
            if (existing == null) {
                summary.copy(
                    items = summary.items + CartItem(
                        productId = productId,
                        name = name,
                        unitPrice = unitPrice,
                        quantity = quantity
                    )
                )
            } else {
                summary.copy(
                    items = summary.items.map { item ->
                        if (item.productId == productId) {
                            item.copy(quantity = item.quantity + quantity)
                        } else {
                            item
                        }
                    }
                )
            }
        }
    }

    override fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        _cart.update { summary ->
            summary.copy(
                items = summary.items.map { item ->
                    if (item.productId == productId) item.copy(quantity = quantity) else item
                }
            )
        }
    }

    override fun removeItem(productId: String) {
        _cart.update { summary ->
            summary.copy(items = summary.items.filterNot { it.productId == productId })
        }
    }

    override fun clear() {
        _cart.update { CartSummary() }
    }
}
