package com.example.orders_feat.data.local

import com.example.database.dao.OrdersDao
import com.example.orders_feat.data.mapper.toDataModel
import com.example.orders_feat.data.mapper.toDomain
import com.example.orders_feat.data.mapper.toEntity
import com.example.orders_feat.data.mapper.toItemEntities
import com.example.orders_lib.models.CreateOrderRequest
import com.example.orders_feat.data.model.OrderDataModel
import com.example.orders_feat.data.model.OrderItemDataModel
import com.example.orders_lib.models.Order
import com.example.orders_lib.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.math.BigDecimal
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomOrdersDataSource @Inject constructor(
    private val ordersDao: OrdersDao
) : OrdersRepository {

    override fun observeOrders(): Flow<List<Order>> = flow {
        emitAll(
            ordersDao.observeOrders().map { orders ->
                orders.map { it.toDataModel().toDomain() }
            }
        )
    }

    override suspend fun createOrder(request: CreateOrderRequest) {
        val nextOrderNumber = 1001 + ordersDao.countOrders()
        val createdAtEpochMillis = System.currentTimeMillis()
        val orderId = "ord-$nextOrderNumber"
        val total = request.items.fold(BigDecimal.ZERO) { acc, item -> acc + item.total }
        val order = OrderDataModel(
            id = orderId,
            orderNumber = "#$nextOrderNumber",
            createdAtLabel = SimpleDateFormat("h:mm a", Locale.US).format(Date(createdAtEpochMillis)),
            createdAtEpochMillis = createdAtEpochMillis,
            total = total.toPlainString(),
            status = request.status,
            customerName = request.customerName,
            notes = request.notes,
            items = request.items.mapIndexed { index, item ->
                OrderItemDataModel(
                    id = "$orderId-item-${index + 1}",
                    name = item.name,
                    quantity = item.quantity,
                    total = item.total.toPlainString()
                )
            }
        )

        ordersDao.insertOrderWithItems(
            order = order.toEntity(),
            items = order.toItemEntities()
        )
    }
}
