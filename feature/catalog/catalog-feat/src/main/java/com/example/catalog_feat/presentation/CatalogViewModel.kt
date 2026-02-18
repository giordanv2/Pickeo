package com.example.catalog_feat.presentation

import androidx.lifecycle.ViewModel
import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface CatalogUiEvent {
    data class SearchChanged(val query: String) : CatalogUiEvent
    data class SectionSelected(val sectionId: String?) : CatalogUiEvent
    data class AddToCartClicked(val item: CatalogItem) : CatalogUiEvent
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

class CatalogViewModel(
    private val initialCatalog: Catalog = CatalogFixtures.sampleCatalog()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    fun onEvent(event: CatalogUiEvent) {
        when (event) {
            is CatalogUiEvent.SearchChanged -> onSearchChanged(event.query)
            is CatalogUiEvent.SectionSelected -> onSectionSelected(event.sectionId)
            is CatalogUiEvent.AddToCartClicked -> Unit
            CatalogUiEvent.RetryClicked -> loadCatalog()
        }
    }

    private fun loadCatalog() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                catalog = initialCatalog,
                selectedSectionId = null,
                visibleItems = initialCatalog.sections.flatMap { section -> section.items }
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
