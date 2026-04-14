package com.example.orders_lib.repository

import com.example.orders_lib.models.Order
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    fun observeOrders(): Flow<List<Order>>
}
