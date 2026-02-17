package com.example.catalog_lib.models

data class CatalogSection(
    val id: String,
    val title: String,
    val items: List<CatalogItem>,
    val gridColumns: Int = DEFAULT_GRID_COLUMNS
) {
    companion object {
        const val DEFAULT_GRID_COLUMNS = 3
    }
}
