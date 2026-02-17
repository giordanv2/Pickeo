package com.example.catalog_feat.presentation

import com.example.catalog_lib.models.CatalogItem

sealed interface CatalogUiEvent {
    data class SearchChanged(val query: String) : CatalogUiEvent
    data class SectionSelected(val sectionId: String?) : CatalogUiEvent
    data class AddToCartClicked(val item: CatalogItem) : CatalogUiEvent
    data object RetryClicked : CatalogUiEvent
}
