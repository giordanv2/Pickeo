package com.example.cart_lib

import com.example.cart_lib.repository.CartRepository
import com.example.cart_lib.repository.InMemoryCartRepository
import com.example.cart_lib.usecase.AddItemToCartUseCase
import com.example.cart_lib.usecase.ClearCartUseCase
import com.example.cart_lib.usecase.ObserveCartUseCase
import com.example.cart_lib.usecase.RemoveCartItemUseCase
import com.example.cart_lib.usecase.UpdateCartItemQuantityUseCase

object CartGraph {
    val repository: CartRepository = InMemoryCartRepository()

    val addItemToCartUseCase = AddItemToCartUseCase(repository)
    val observeCartUseCase = ObserveCartUseCase(repository)
    val updateCartItemQuantityUseCase = UpdateCartItemQuantityUseCase(repository)
    val removeCartItemUseCase = RemoveCartItemUseCase(repository)
    val clearCartUseCase = ClearCartUseCase(repository)
}
