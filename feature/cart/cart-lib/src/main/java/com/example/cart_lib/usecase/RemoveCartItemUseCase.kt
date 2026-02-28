package com.example.cart_lib.usecase

import com.example.cart_lib.repository.CartRepository
import javax.inject.Inject

class RemoveCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: String) {
        repository.removeItem(productId)
    }
}
