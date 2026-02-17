package com.example.catalog_lib.models

import java.math.BigDecimal

data class CatalogItem(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
)
