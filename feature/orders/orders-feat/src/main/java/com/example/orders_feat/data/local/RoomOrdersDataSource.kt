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
        seedIfEmpty()
        emitAll(
            ordersDao.observeOrders().map { orders ->
                orders.map { it.toDataModel().toDomain() }
            }
        )
    }

    override suspend fun createOrder(request: CreateOrderRequest) {
        seedIfEmpty()

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

    private suspend fun seedIfEmpty() {
        if (ordersDao.countOrders() > 0) return
        val sampleOrders = sampleOrders()
        ordersDao.insertOrders(sampleOrders.map { it.toEntity() })
        ordersDao.insertOrderItems(sampleOrders.flatMap { it.toItemEntities() })
    }
}

private fun sampleOrders(): List<OrderDataModel> = listOf(
    OrderDataModel(
        id = "ord-1001",
        orderNumber = "#1001",
        createdAtLabel = "8:42 AM",
        createdAtEpochMillis = 1_744_624_120_000,
        total = BigDecimal("16.25").toPlainString(),
        status = "Ready",
        customerName = "Avery Johnson",
        notes = "Extra hot latte and pack pastry separately.",
        items = listOf(
            OrderItemDataModel("1", "Espresso", 2, BigDecimal("5.00").toPlainString()),
            OrderItemDataModel("2", "Butter Croissant", 1, BigDecimal("3.75").toPlainString()),
            OrderItemDataModel("3", "Caffe Latte", 1, BigDecimal("7.50").toPlainString())
        )
    ),
    OrderDataModel(
        id = "ord-1002",
        orderNumber = "#1002",
        createdAtLabel = "9:05 AM",
        createdAtEpochMillis = 1_744_625_500_000,
        total = BigDecimal("11.50").toPlainString(),
        status = "In Progress",
        customerName = "Morgan Lee",
        notes = "Customer will pick up at front counter.",
        items = listOf(
            OrderItemDataModel("4", "Americano", 1, BigDecimal("3.00").toPlainString()),
            OrderItemDataModel("5", "Blueberry Muffin", 1, BigDecimal("3.50").toPlainString()),
            OrderItemDataModel("6", "Caffe Latte", 1, BigDecimal("5.00").toPlainString())
        )
    ),
    OrderDataModel(
        id = "ord-1003",
        orderNumber = "#1003",
        createdAtLabel = "9:18 AM",
        createdAtEpochMillis = 1_744_626_280_000,
        total = BigDecimal("8.75").toPlainString(),
        status = "Completed",
        customerName = "Jordan Smith",
        items = listOf(
            OrderItemDataModel("7", "Americano", 1, BigDecimal("3.00").toPlainString()),
            OrderItemDataModel("8", "Butter Croissant", 1, BigDecimal("5.75").toPlainString())
        )
    )
)
