package com.example.orders_feat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.usecase.AddItemToCartUseCase
import com.example.cart_lib.usecase.ClearCartUseCase
import com.example.cart_lib.usecase.ObserveCartUseCase
import com.example.orders_lib.models.CreateOrderItem
import com.example.orders_lib.models.CreateOrderRequest
import com.example.orders_lib.models.Order
import com.example.orders_lib.models.OrderItem
import com.example.orders_lib.usecase.CreateOrderUseCase
import com.example.orders_lib.usecase.GetOrderUseCase
import com.example.orders_lib.usecase.ObserveOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.RoundingMode
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val selectedOrderId: String? = null,
    val isLoadingOrderIntoCart: Boolean = false,
)

sealed interface OrdersUiEvent {
    data class OrderSelected(val orderId: String) : OrdersUiEvent
    data object LoadSelectedOrderClicked : OrdersUiEvent
}

sealed interface OrdersNavigationEvent {
    data object OpenCart : OrdersNavigationEvent
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val getOrderUseCase: GetOrderUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val addItemToCartUseCase: AddItemToCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()
    private val _navigationEvents = MutableSharedFlow<OrdersNavigationEvent>()
    val navigationEvents: SharedFlow<OrdersNavigationEvent> = _navigationEvents.asSharedFlow()
    private var currentCartSummary: CartSummary = CartSummary()

    init {
        viewModelScope.launch {
            observeOrdersUseCase().collect { orders ->
                _uiState.update { current ->
                    val selectedOrder = getOrderUseCase(orders, current.selectedOrderId)
                    current.copy(
                        isLoading = false,
                        orders = orders,
                        selectedOrderId = selectedOrder?.id
                    )
                }
            }
        }

        viewModelScope.launch {
            observeCartUseCase().collect { summary ->
                currentCartSummary = summary
            }
        }
    }

    fun onEvent(event: OrdersUiEvent) {
        when (event) {
            is OrdersUiEvent.OrderSelected -> {
                _uiState.update { it.copy(selectedOrderId = event.orderId) }
            }
            OrdersUiEvent.LoadSelectedOrderClicked -> loadSelectedOrderIntoCart()
        }
    }

    private fun loadSelectedOrderIntoCart() {
        val state = uiState.value
        if (state.isLoadingOrderIntoCart) return

        val order = getOrderUseCase(state.orders, state.selectedOrderId) ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingOrderIntoCart = true) }
            try {
                autoSaveCurrentCartIfNeeded(order.orderNumber)
                clearCartUseCase()
                order.items.forEach { item ->
                    addItemToCartUseCase(
                        productId = "loaded-order::${order.orderNumber}::${item.id}",
                        name = item.name,
                        unitPrice = item.unitPrice(),
                        quantity = item.quantity
                    )
                }
                _navigationEvents.emit(OrdersNavigationEvent.OpenCart)
            } finally {
                _uiState.update { it.copy(isLoadingOrderIntoCart = false) }
            }
        }
    }

    private suspend fun autoSaveCurrentCartIfNeeded(targetOrderNumber: String) {
        val summary = currentCartSummary
        if (summary.items.isEmpty()) return

        createOrderUseCase(
            CreateOrderRequest(
                items = summary.items.map { item ->
                    CreateOrderItem(
                        name = item.name,
                        quantity = item.quantity,
                        total = item.lineTotal
                    )
                },
                notes = "Auto-saved from cart before loading $targetOrderNumber"
            )
        )
    }

    private fun OrderItem.unitPrice() =
        if (quantity > 0) {
            total.divide(quantity.toBigDecimal(), 2, RoundingMode.HALF_UP)
        } else {
            java.math.BigDecimal.ZERO
        }
}
