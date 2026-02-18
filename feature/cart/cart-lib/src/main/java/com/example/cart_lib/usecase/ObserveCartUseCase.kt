package com.example.cart_lib.usecase

import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.repository.CartRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveCartUseCase(
    private val repository: CartRepository
) {
    operator fun invoke(): StateFlow<CartSummary> = repository.cart
}
