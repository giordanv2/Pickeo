package com.example.order_entry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.usecase.AddItemToCartUseCase
import com.example.cart_lib.usecase.ObserveCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderEntryUiState(
    val cartSummary: CartSummary = CartSummary()
)

@HiltViewModel
class OrderEntryViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val addItemToCartUseCase: AddItemToCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderEntryUiState())
    val uiState: StateFlow<OrderEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCartUseCase().collect { summary ->
                _uiState.update { it.copy(cartSummary = summary) }
            }
        }
    }

    fun onCatalogItemAdded(
        productId: String,
        name: String,
        unitPrice: BigDecimal
    ) {
        viewModelScope.launch {
            addItemToCartUseCase(
                productId = productId,
                name = name,
                unitPrice = unitPrice
            )
        }
    }
}
