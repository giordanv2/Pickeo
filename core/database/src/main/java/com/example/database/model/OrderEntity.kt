package com.example.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val createdAtLabel: String,
    val createdAtEpochMillis: Long,
    val total: String,
    val status: String,
    val customerName: String,
    val notes: String
)
