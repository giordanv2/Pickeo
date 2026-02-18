package com.example.cart_lib.usecase

import com.example.cart_lib.repository.CartRepository

class RemoveCartItemUseCase(
    private val repository: CartRepository
) {
    operator fun invoke(productId: String) {
        repository.removeItem(productId)
    }
}
