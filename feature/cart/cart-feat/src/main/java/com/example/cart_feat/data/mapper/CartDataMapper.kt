package com.example.cart_feat.data.mapper

import com.example.cart_feat.data.model.CartDataModel
import com.example.cart_lib.models.CartItem
import com.example.cart_lib.models.CartSummary
import com.example.database.model.CartItemEntity

fun CartItemEntity.toDataModel(): CartDataModel {
    return CartDataModel(
        productId = productId,
        name = name,
        unitPrice = unitPrice.toBigDecimal(),
        quantity = quantity,
        addedAt = addedAt
    )
}

fun CartDataModel.toEntity(): CartItemEntity {
    return CartItemEntity(
        productId = productId,
        name = name,
        unitPrice = unitPrice.toPlainString(),
        quantity = quantity,
        addedAt = addedAt
    )
}

fun CartDataModel.toDomain(): CartItem {
    return CartItem(
        productId = productId,
        name = name,
        unitPrice = unitPrice,
        quantity = quantity
    )
}

fun List<CartDataModel>.toDomainSummary(): CartSummary {
    return CartSummary(items = map { it.toDomain() })
}
