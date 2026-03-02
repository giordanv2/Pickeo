package com.example.cart_lib.repository

import com.example.cart_lib.models.CartItem
import com.example.cart_lib.models.CartSummary
import com.example.database.dao.CartItemDao
import com.example.database.model.CartItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRoomDataSource @Inject constructor(
    private val cartItemDao: CartItemDao
) : CartRepository {

    override fun observeCart(): Flow<CartSummary> {
        return cartItemDao.observeAll().map { entities ->
            CartSummary(items = entities.map { entity -> entity.toDomain() })
        }
    }

    override suspend fun addItem(
        productId: String,
        name: String,
        unitPrice: BigDecimal,
        quantity: Int
    ) {
        if (quantity <= 0) return
        val existing = cartItemDao.findById(productId)
        val nextQuantity = (existing?.quantity ?: 0) + quantity
        cartItemDao.upsert(
            CartItemEntity(
                productId = productId,
                name = name,
                unitPrice = unitPrice.toPlainString(),
                quantity = nextQuantity
            )
        )
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        val existing = cartItemDao.findById(productId) ?: return
        cartItemDao.upsert(existing.copy(quantity = quantity))
    }

    override suspend fun removeItem(productId: String) {
        cartItemDao.deleteById(productId)
    }

    override suspend fun clear() {
        cartItemDao.clear()
    }
}

private fun CartItemEntity.toDomain(): CartItem {
    return CartItem(
        productId = productId,
        name = name,
        unitPrice = unitPrice.toBigDecimal(),
        quantity = quantity
    )
}
