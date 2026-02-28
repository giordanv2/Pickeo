package com.example.cart_lib.usecase

import com.example.cart_lib.repository.CartRepository
import javax.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke() {
        repository.clear()
    }
}
