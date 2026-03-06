package com.example.cart_feat.data.model

import java.math.BigDecimal

data class CartDataModel(
    val productId: String,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val addedAt: Long
)
