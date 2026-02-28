package com.example.cart_lib.usecase

import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<CartSummary> = repository.observeCart()
}
