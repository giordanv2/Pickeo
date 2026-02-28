package com.example.cart_feat.presentation

import androidx.lifecycle.ViewModel
import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.usecase.ClearCartUseCase
import com.example.cart_lib.usecase.ObserveCartUseCase
import com.example.cart_lib.usecase.RemoveCartItemUseCase
import com.example.cart_lib.usecase.UpdateCartItemQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class CartViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase,
    private val clearCartUseCase: ClearCartUseCase
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
            is CartUiEvent.RemoveClicked -> viewModelScope.launch {
                removeCartItemUseCase(event.productId)
            }
            CartUiEvent.ClearClicked -> viewModelScope.launch {
                clearCartUseCase()
            }
        }
    }

    private fun incrementItem(productId: String) {
        val item = uiState.value.summary.items.firstOrNull { it.productId == productId } ?: return
        viewModelScope.launch {
            updateCartItemQuantityUseCase(productId, item.quantity + 1)
        }
    }

    private fun decrementItem(productId: String) {
        val item = uiState.value.summary.items.firstOrNull { it.productId == productId } ?: return
        viewModelScope.launch {
            updateCartItemQuantityUseCase(productId, item.quantity - 1)
        }
    }
}
