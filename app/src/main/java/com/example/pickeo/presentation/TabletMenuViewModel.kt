package com.example.pickeo.presentation

import androidx.lifecycle.ViewModel
import com.example.pickeo.domain.models.CartLine
import com.example.pickeo.domain.models.MenuItem
import com.example.pickeo.domain.models.MenuSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode

data class TabletMenuUiState(
    val sections: List<MenuSection> = emptyList(),
    val cart: List<CartLine> = emptyList(),
    val amountReceived: String = ""
) {
    val total: BigDecimal = cart.fold(BigDecimal.ZERO) { acc, line ->
        acc + line.item.price.multiply(BigDecimal(line.quantity))
    }.setScale(2, RoundingMode.HALF_UP)

    val change: BigDecimal = amountReceived.toBigDecimalOrNull()
        ?.minus(total)
        ?.setScale(2, RoundingMode.HALF_UP)
        ?: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
}

class TabletMenuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TabletMenuUiState(sections = demoSections()))
    val uiState: StateFlow<TabletMenuUiState> = _uiState

    fun addItem(item: MenuItem) {
        _uiState.update { state ->
            val existing = state.cart.find { it.item.id == item.id }
            val newCart =
                if (existing == null) state.cart + CartLine(item, 1)
                else state.cart.map {
                    if (it.item.id == item.id) it.copy(quantity = it.quantity + 1) else it
                }
            state.copy(cart = newCart)
        }
    }

    fun decrementItem(item: MenuItem) {
        _uiState.update { state ->
            val newCart = state.cart.mapNotNull {
                if (it.item.id == item.id) {
                    val newQty = it.quantity - 1
                    if (newQty > 0) it.copy(quantity = newQty) else null
                } else it
            }
            state.copy(cart = newCart)
        }
    }

    fun removeLine(item: MenuItem) {
        _uiState.update { state ->
            state.copy(cart = state.cart.filterNot { it.item.id == item.id })
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = emptyList()) }
    }

    fun updateAmountReceived(text: String) {
        // Permitimos solo números y punto decimal básico
        val sanitized = text.replace(",", ".")
        _uiState.update { it.copy(amountReceived = sanitized) }
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        try { BigDecimal(this) } catch (_: Exception) { null }

    companion object {
        private fun demoSections(): List<MenuSection> {
            fun d(price: Double) = BigDecimal(price).setScale(2, RoundingMode.HALF_UP)

            val obleas = listOf(
                MenuItem(name = "Burger", price = d(5.99)),
                MenuItem(name = "Pizza", price = d(7.49)),
                MenuItem(name = "Fries", price = d(2.99)),
                MenuItem(name = "Soda", price = d(1.49)),
                MenuItem(name = "Hot Dog", price = d(3.99)),
                MenuItem(name = "Salad", price = d(4.99)),
                MenuItem(name = "Chicken Nuggets", price = d(6.49)),
            )
            val drinks = listOf(
                MenuItem(name = "Soda", price = d(1.49)),
                MenuItem(name = "Milkshake", price = d(3.49)),
                MenuItem(name = "Water Bottle", price = d(1.00)),
                MenuItem(name = "Coffee", price = d(2.00)),
                MenuItem(name = "Iced Tea", price = d(2.25)),
                MenuItem(name = "Smoothie", price = d(4.00)),
            )
            val desserts = listOf(
                MenuItem(name = "Ice Cream", price = d(2.75)),
                MenuItem(name = "Cheesecake", price = d(4.25)),
                MenuItem(name = "Brownie", price = d(3.50)),
                MenuItem(name = "Cupcake", price = d(2.00)),
                MenuItem(name = "Donut", price = d(1.80)),
                MenuItem(name = "Apple Pie", price = d(3.95)),
            )

            return listOf(
                MenuSection("Obleas", obleas),
                MenuSection("Drinks", drinks),
                MenuSection("Desserts", desserts),
            )
        }
    }
}