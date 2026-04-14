package com.example.orders_lib.models

import java.math.BigDecimal

data class Order(
    val id: String,
    val orderNumber: String,
    val createdAtLabel: String,
    val total: BigDecimal,
    val status: String,
    val customerName: String,
    val notes: String = "",
    val items: List<OrderItem> = emptyList(),
)
