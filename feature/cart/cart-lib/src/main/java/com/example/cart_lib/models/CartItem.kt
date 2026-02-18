package com.example.cart_lib.models

import java.math.BigDecimal

data class CartItem(
    val productId: String,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) {
    val lineTotal: BigDecimal
        get() = unitPrice.multiply(quantity.toBigDecimal())
}
