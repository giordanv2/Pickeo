package com.example.catalog_feat.presentation

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.example.cart_lib.usecase.AddItemToCartUseCase
import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import com.example.catalog_lib.domain.usecase.CreateCatalogItemUseCase
import com.example.catalog_lib.domain.usecase.ObserveCatalogUseCase
import com.example.catalog_lib.domain.usecase.RemoveCatalogItemUseCase
import com.example.catalog_lib.domain.usecase.ReorderCatalogItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CatalogUiEvent {
    data class SearchChanged(val query: String) : CatalogUiEvent
    data class SectionSelected(val sectionId: String?) : CatalogUiEvent
    data class AddToCartClicked(val item: CatalogItem) : CatalogUiEvent
    data object CreateMockCatalogItemClicked : CatalogUiEvent
    data class CreateCatalogItemSubmitted(
        val name: String,
        val price: String,
        val sectionTitle: String
    ) : CatalogUiEvent
    data object EnterEditModeClicked : CatalogUiEvent
    data object CancelEditModeClicked : CatalogUiEvent
    data object ConfirmEditModeClicked : CatalogUiEvent
    data class ReorderItemMoved(val fromIndex: Int, val toIndex: Int) : CatalogUiEvent
    data class DeleteCatalogItemClicked(val itemId: String) : CatalogUiEvent
    data object RetryClicked : CatalogUiEvent
}

data class CatalogUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val catalog: Catalog? = null,
    val selectedSectionId: String? = null,
    val searchQuery: String = "",
    val visibleItems: List<CatalogItem> = emptyList(),
    val isEditMode: Boolean = false,
    val editableItems: List<CatalogItem> = emptyList(),
) {
    val sections: List<CatalogSection>
        get() = catalog?.sections.orEmpty()
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val observeCatalogUseCase: ObserveCatalogUseCase,
    private val createCatalogItemUseCase: CreateCatalogItemUseCase,
    private val reorderCatalogItemsUseCase: ReorderCatalogItemsUseCase,
    private val removeCatalogItemUseCase: RemoveCatalogItemUseCase,
    private val addItemToCartUseCase: AddItemToCartUseCase
) : ViewModel() {
    private var mockItemCounter = 0

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        observeCatalog()
    }

    fun onEvent(event: CatalogUiEvent) {
        when (event) {
            is CatalogUiEvent.SearchChanged -> onSearchChanged(event.query)
            is CatalogUiEvent.SectionSelected -> onSectionSelected(event.sectionId)
            is CatalogUiEvent.AddToCartClicked -> addItemToCart(event)
            CatalogUiEvent.CreateMockCatalogItemClicked -> createMockCatalogItem()
            is CatalogUiEvent.CreateCatalogItemSubmitted -> createCatalogItem(event)
            CatalogUiEvent.EnterEditModeClicked -> enterEditMode()
            CatalogUiEvent.CancelEditModeClicked -> cancelEditMode()
            CatalogUiEvent.ConfirmEditModeClicked -> confirmEditMode()
            is CatalogUiEvent.ReorderItemMoved -> moveEditableItem(event.fromIndex, event.toIndex)
            is CatalogUiEvent.DeleteCatalogItemClicked -> deleteCatalogItem(event.itemId)
            CatalogUiEvent.RetryClicked -> Unit
        }
    }

    private fun deleteCatalogItem(itemId: String) {
        if (itemId.isBlank()) return
        _uiState.update { state ->
            state.copy(
                visibleItems = state.visibleItems.filterNot { it.id == itemId },
                editableItems = state.editableItems.filterNot { it.id == itemId }
            )
        }
        viewModelScope.launch {
            removeCatalogItemUseCase(itemId)
        }
    }

    private fun addItemToCart(event: CatalogUiEvent.AddToCartClicked) {
        viewModelScope.launch {
            addItemToCartUseCase(
                productId = event.item.id,
                name = event.item.name,
                unitPrice = event.item.price,
            )
        }
    }

    private fun observeCatalog() {
        viewModelScope.launch {
            observeCatalogUseCase().collect { catalog ->
                _uiState.update { state ->
                    val safeSelectedSectionId = state.selectedSectionId
                        ?.takeIf { sectionId -> catalog.sections.any { it.id == sectionId } }
                    val visibleItems = buildVisibleItems(
                        sections = catalog.sections,
                        selectedSectionId = safeSelectedSectionId,
                        query = state.searchQuery.trim()
                    )
                    state.copy(
                        isLoading = false,
                        errorMessage = null,
                        catalog = catalog,
                        selectedSectionId = safeSelectedSectionId,
                        visibleItems = visibleItems,
                        editableItems = if (state.isEditMode) state.editableItems else emptyList()
                    )
                }
            }
        }
    }

    private fun createCatalogItem(event: CatalogUiEvent.CreateCatalogItemSubmitted) {
        val name = event.name.trim()
        val sectionTitle = event.sectionTitle.trim()
        val price = event.price.trim().toBigDecimalOrNull()

        if (name.isEmpty() || sectionTitle.isEmpty() || price == null || price <= BigDecimal.ZERO) {
            return
        }

        viewModelScope.launch {
            createCatalogItemUseCase(
                name = name,
                unitPrice = price,
                sectionTitle = sectionTitle
            )
        }
    }

    private fun createMockCatalogItem() {
        viewModelScope.launch {
            mockItemCounter += 1
            val counter = mockItemCounter
            val sectionTitle = _uiState.value.sections.firstOrNull()?.title ?: "Debug"
            createCatalogItemUseCase(
                name = "Mock item $counter",
                unitPrice = "${(counter % 7) + 1}.99".toBigDecimal(),
                sectionTitle = sectionTitle
            )
        }
    }

    private fun onSearchChanged(query: String) {
        _uiState.update { state ->
            if (state.isEditMode) return@update state
            val trimmed = query.trim()
            state.copy(
                searchQuery = query,
                visibleItems = buildVisibleItems(
                    sections = state.sections,
                    selectedSectionId = state.selectedSectionId,
                    query = trimmed
                )
            )
        }
    }

    private fun onSectionSelected(sectionId: String?) {
        _uiState.update { state ->
            if (state.isEditMode) return@update state
            state.copy(
                selectedSectionId = sectionId,
                visibleItems = buildVisibleItems(
                    sections = state.sections,
                    selectedSectionId = sectionId,
                    query = state.searchQuery.trim()
                )
            )
        }
    }

    private fun buildVisibleItems(
        sections: List<CatalogSection>,
        selectedSectionId: String?,
        query: String
    ): List<CatalogItem> {
        val selectedSectionItems = if (selectedSectionId == null) {
            sections.flatMap { it.items }
        } else {
            sections.firstOrNull { it.id == selectedSectionId }?.items.orEmpty()
        }

        if (query.isEmpty()) return selectedSectionItems
        return selectedSectionItems.filter { item ->
            item.name.contains(query, ignoreCase = true) ||
                item.id.contains(query, ignoreCase = true)
        }
    }

    private fun enterEditMode() {
        _uiState.update { state ->
            if (state.isEditMode || state.visibleItems.isEmpty()) return@update state
            state.copy(
                isEditMode = true,
                editableItems = state.visibleItems
            )
        }
    }

    private fun cancelEditMode() {
        _uiState.update { state ->
            state.copy(
                isEditMode = false,
                editableItems = emptyList()
            )
        }
    }

    private fun moveEditableItem(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            if (!state.isEditMode) return@update state
            if (fromIndex !in state.editableItems.indices || toIndex !in state.editableItems.indices) {
                return@update state
            }
            val mutableItems = state.editableItems.toMutableList()
            val moved = mutableItems.removeAt(fromIndex)
            mutableItems.add(toIndex, moved)
            state.copy(editableItems = mutableItems)
        }
    }

    private fun confirmEditMode() {
        val currentState = _uiState.value
        val editableIds = currentState.editableItems.map { it.id }
        if (editableIds.isEmpty()) {
            cancelEditMode()
            return
        }

        val baseCatalogOrder = currentState.sections.flatMap { it.items }.map { it.id }
        val idsToPersist = if (editableIds.size == baseCatalogOrder.size) {
            editableIds
        } else {
            val editableIdSet = editableIds.toSet()
            val reorderedEditable = ArrayDeque(editableIds)
            baseCatalogOrder.map { id ->
                if (editableIdSet.contains(id)) reorderedEditable.removeFirst() else id
            }
        }

        viewModelScope.launch {
            reorderCatalogItemsUseCase(idsToPersist)
            _uiState.update { state ->
                state.copy(
                    isEditMode = false,
                    editableItems = emptyList()
                )
            }
        }
    }
}
