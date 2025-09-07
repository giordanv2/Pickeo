package com.example.pickeo.domain.models

import java.math.BigDecimal
import java.util.UUID

data class MenuItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: BigDecimal
)
