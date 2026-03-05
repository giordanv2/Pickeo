package com.example.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val unitPrice: String,
    val sectionId: String,
    val sectionTitle: String,
    val isAvailable: Boolean,
    val createdAt: Long
)
