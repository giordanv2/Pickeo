package com.example.cart_feat.presentation

import androidx.lifecycle.ViewModel
import com.example.cart_lib.CartGraph
import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.usecase.ClearCartUseCase
import com.example.cart_lib.usecase.ObserveCartUseCase
import com.example.cart_lib.usecase.RemoveCartItemUseCase
import com.example.cart_lib.usecase.UpdateCartItemQuantityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface CartUiEvent {
    data class IncrementClicked(val productId: String) : CartUiEvent
    data class DecrementClicked(val productId: String) : CartUiEvent
    data class RemoveClicked(val productId: String) : CartUiEvent
    data object ClearClicked : CartUiEvent
}

data class CartUiState(
    val isLoading: Boolean = true,
    val summary: CartSummary = CartSummary()
)

class CartViewModel(
    private val observeCartUseCase: ObserveCartUseCase = CartGraph.observeCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase =
        CartGraph.updateCartItemQuantityUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase = CartGraph.removeCartItemUseCase,
    private val clearCartUseCase: ClearCartUseCase = CartGraph.clearCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCartUseCase().collect { summary ->
                _uiState.update {
                    it.copy(isLoading = false, summary = summary)
                }
            }
        }
    }

    fun onEvent(event: CartUiEvent) {
        when (event) {
            is CartUiEvent.IncrementClicked -> incrementItem(event.productId)
            is CartUiEvent.DecrementClicked -> decrementItem(event.productId)
            is CartUiEvent.RemoveClicked -> removeCartItemUseCase(event.productId)
            CartUiEvent.ClearClicked -> clearCartUseCase()
        }
    }

    private fun incrementItem(productId: String) {
        val item = observeCartUseCase().value.items.firstOrNull { it.productId == productId } ?: return
        updateCartItemQuantityUseCase(productId, item.quantity + 1)
    }

    private fun decrementItem(productId: String) {
        val item = observeCartUseCase().value.items.firstOrNull { it.productId == productId } ?: return
        updateCartItemQuantityUseCase(productId, item.quantity - 1)
    }
}
