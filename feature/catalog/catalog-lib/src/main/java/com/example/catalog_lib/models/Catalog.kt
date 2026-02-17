package com.example.catalog_lib.models

data class Catalog(
    val id: String,
    val name: String,
    val sections: List<CatalogSection>
)
