package com.example.orders_feat.data.model

data class OrderDataModel(
    val id: String,
    val orderNumber: String,
    val createdAtLabel: String,
    val createdAtEpochMillis: Long,
    val total: String,
    val status: String,
    val customerName: String,
    val notes: String = "",
    val items: List<OrderItemDataModel>,
)

data class OrderItemDataModel(
    val id: String,
    val name: String,
    val quantity: Int,
    val total: String,
)
