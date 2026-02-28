package com.example.cart_lib.usecase

import com.example.cart_lib.repository.CartRepository
import java.math.BigDecimal
import javax.inject.Inject

class AddItemToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(
        productId: String,
        name: String,
        unitPrice: BigDecimal,
        quantity: Int = 1
    ) {
        repository.addItem(productId, name, unitPrice, quantity)
    }
}
