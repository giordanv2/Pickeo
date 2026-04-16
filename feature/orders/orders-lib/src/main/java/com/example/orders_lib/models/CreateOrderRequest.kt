package com.example.orders_lib.models

data class CreateOrderRequest(
    val items: List<CreateOrderItem>,
    val customerName: String = "Walk-in Customer",
    val status: String = "In Progress",
    val notes: String = "",
)

data class CreateOrderItem(
    val name: String,
    val quantity: Int,
    val total: java.math.BigDecimal,
)
