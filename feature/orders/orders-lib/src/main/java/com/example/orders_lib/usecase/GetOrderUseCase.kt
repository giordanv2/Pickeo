package com.example.orders_lib.usecase

import com.example.orders_lib.models.Order
import javax.inject.Inject

class GetOrderUseCase @Inject constructor() {
    operator fun invoke(
        orders: List<Order>,
        orderId: String?
    ): Order? = orders.firstOrNull { it.id == orderId } ?: orders.firstOrNull()
}
