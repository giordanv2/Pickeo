package com.example.cart_feat.data.local

import com.example.cart_feat.data.mapper.toDataModel
import com.example.cart_feat.data.mapper.toDomainSummary
import com.example.cart_feat.data.mapper.toEntity
import com.example.cart_feat.data.model.CartDataModel
import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.repository.CartRepository
import com.example.database.dao.CartItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCartDataSource @Inject constructor(
    private val cartItemDao: CartItemDao
) : CartRepository {

    override fun observeCart(): Flow<CartSummary> {
        return cartItemDao.observeAll().map { entities ->
            entities.map { it.toDataModel() }.toDomainSummary()
        }
    }

    override suspend fun addItem(
        productId: String,
        name: String,
        unitPrice: BigDecimal,
        quantity: Int
    ) {
        if (quantity <= 0) return

        val item = CartDataModel(
            productId = productId,
            name = name,
            unitPrice = unitPrice,
            quantity = quantity,
            addedAt = System.currentTimeMillis()
        )

        val insertResult = cartItemDao.insertIgnore(item.toEntity())
        if (insertResult == -1L) {
            val existing = cartItemDao.findById(productId)?.toDataModel() ?: return
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
