package com.example.orders_lib.models

import java.math.BigDecimal

data class OrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val total: BigDecimal,
)
