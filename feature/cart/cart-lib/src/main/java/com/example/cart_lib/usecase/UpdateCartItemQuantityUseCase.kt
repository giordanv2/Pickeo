package com.example.cart_lib.usecase

import com.example.cart_lib.repository.CartRepository

class UpdateCartItemQuantityUseCase(
    private val repository: CartRepository
) {
    operator fun invoke(productId: String, quantity: Int) {
        repository.updateQuantity(productId, quantity)
    }
}
