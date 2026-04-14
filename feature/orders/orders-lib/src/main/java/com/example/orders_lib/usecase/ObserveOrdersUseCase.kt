package com.example.orders_lib.usecase

import com.example.orders_lib.models.Order
import com.example.orders_lib.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrdersUseCase @Inject constructor(
    private val repository: OrdersRepository
) {
    operator fun invoke(): Flow<List<Order>> = repository.observeOrders()
}
