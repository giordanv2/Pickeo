package com.example.orders_feat.data.mapper

import com.example.orders_feat.data.model.OrderDataModel
import com.example.database.model.OrderEntity
import com.example.database.model.OrderItemEntity
import com.example.database.model.OrderWithItems
import com.example.orders_lib.models.Order
import com.example.orders_lib.models.OrderItem
import java.math.BigDecimal

fun OrderDataModel.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    createdAtLabel = createdAtLabel,
    total = total.toBigDecimal(),
    status = status,
    customerName = customerName,
    notes = notes,
    items = items.map { item ->
        OrderItem(
            id = item.id,
            name = item.name,
            quantity = item.quantity,
            total = item.total.toBigDecimal()
        )
    }
)

fun OrderDataModel.toEntity(): OrderEntity = OrderEntity(
    id = id,
    orderNumber = orderNumber,
    createdAtLabel = createdAtLabel,
    createdAtEpochMillis = createdAtEpochMillis,
    total = total,
    status = status,
    customerName = customerName,
    notes = notes
)

fun OrderDataModel.toItemEntities(): List<OrderItemEntity> = items.map { item ->
    OrderItemEntity(
        id = item.id,
        orderId = id,
        name = item.name,
        quantity = item.quantity,
        total = item.total
    )
}

fun OrderWithItems.toDataModel(): OrderDataModel = OrderDataModel(
    id = order.id,
    orderNumber = order.orderNumber,
    createdAtLabel = order.createdAtLabel,
    createdAtEpochMillis = order.createdAtEpochMillis,
    total = order.total,
    status = order.status,
    customerName = order.customerName,
    notes = order.notes,
    items = items.map { item ->
        com.example.orders_feat.data.model.OrderItemDataModel(
            id = item.id,
            name = item.name,
            quantity = item.quantity,
            total = item.total
        )
    }
)
