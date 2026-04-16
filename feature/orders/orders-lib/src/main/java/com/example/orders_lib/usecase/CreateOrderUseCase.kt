package com.example.orders_lib.usecase

import com.example.orders_lib.models.CreateOrderRequest
import com.example.orders_lib.repository.OrdersRepository
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repository: OrdersRepository
) {
    suspend operator fun invoke(request: CreateOrderRequest) {
        if (request.items.isEmpty()) return
        repository.createOrder(request)
    }
}
