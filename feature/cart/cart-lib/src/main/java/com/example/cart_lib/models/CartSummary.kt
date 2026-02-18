package com.example.cart_lib.models

import java.math.BigDecimal

data class CartSummary(
    val items: List<CartItem> = emptyList()
) {
    val totalItems: Int
        get() = items.sumOf { it.quantity }

    val subtotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.lineTotal) }
}
