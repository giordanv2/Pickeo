package com.example.cart_lib.usecase

import com.example.cart_lib.repository.CartRepository

class ClearCartUseCase(
    private val repository: CartRepository
) {
    operator fun invoke() {
        repository.clear()
    }
}
