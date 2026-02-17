package com.example.catalog_feat.presentation

import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection

data class CatalogUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val catalog: Catalog? = null,
    val selectedSectionId: String? = null,
    val searchQuery: String = "",
    val visibleItems: List<CatalogItem> = emptyList(),
    val cartItemCount: Int = 0
) {
    val sections: List<CatalogSection>
        get() = catalog?.sections.orEmpty()
}
