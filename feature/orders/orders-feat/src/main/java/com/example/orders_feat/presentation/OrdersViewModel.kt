package com.example.orders_feat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orders_lib.models.Order
import com.example.orders_lib.usecase.GetOrderUseCase
import com.example.orders_lib.usecase.ObserveOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val selectedOrderId: String? = null,
)

sealed interface OrdersUiEvent {
    data class OrderSelected(val orderId: String) : OrdersUiEvent
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val getOrderUseCase: GetOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

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
    }

    fun onEvent(event: OrdersUiEvent) {
        when (event) {
            is OrdersUiEvent.OrderSelected -> {
                _uiState.update { it.copy(selectedOrderId = event.orderId) }
            }
        }
    }
}
