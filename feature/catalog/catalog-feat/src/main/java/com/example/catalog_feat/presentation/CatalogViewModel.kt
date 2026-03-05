package com.example.catalog_feat.presentation

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import com.example.catalog_lib.domain.usecase.CreateCatalogItemUseCase
import com.example.catalog_lib.domain.usecase.ObserveCatalogUseCase
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
    data class CreateCatalogItemSubmitted(
        val name: String,
        val price: String,
        val sectionTitle: String
    ) : CatalogUiEvent
    data object RetryClicked : CatalogUiEvent
}

data class CatalogUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val catalog: Catalog? = null,
    val selectedSectionId: String? = null,
    val searchQuery: String = "",
    val visibleItems: List<CatalogItem> = emptyList(),
) {
    val sections: List<CatalogSection>
        get() = catalog?.sections.orEmpty()
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val observeCatalogUseCase: ObserveCatalogUseCase,
    private val createCatalogItemUseCase: CreateCatalogItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        observeCatalog()
    }

    fun onEvent(event: CatalogUiEvent) {
        when (event) {
            is CatalogUiEvent.SearchChanged -> onSearchChanged(event.query)
            is CatalogUiEvent.SectionSelected -> onSectionSelected(event.sectionId)
            is CatalogUiEvent.AddToCartClicked -> Unit
            is CatalogUiEvent.CreateCatalogItemSubmitted -> createCatalogItem(event)
            CatalogUiEvent.RetryClicked -> Unit
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
                        visibleItems = visibleItems
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

    private fun onSearchChanged(query: String) {
        _uiState.update { state ->
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
}
