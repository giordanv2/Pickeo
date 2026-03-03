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
        val insertResult = cartItemDao.insertIgnore(
            CartItemEntity(
                productId = productId,
                name = name,
                unitPrice = unitPrice.toPlainString(),
                quantity = quantity,
                addedAt = System.currentTimeMillis()
            )
        )

        // Existing rows are updated in place to preserve row identity and ordering.
        if (insertResult == -1L) {
            val existing = cartItemDao.findById(productId) ?: return
            cartItemDao.updateItem(
                productId = productId,
                name = name,
                unitPrice = unitPrice.toPlainString(),
                quantity = existing.quantity + quantity
            )
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        cartItemDao.updateQuantity(productId, quantity)
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
