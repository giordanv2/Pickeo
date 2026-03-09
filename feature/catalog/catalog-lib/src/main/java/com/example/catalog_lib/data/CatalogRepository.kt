package com.example.catalog_lib.data

import com.example.catalog_lib.models.Catalog
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CatalogRepository {
    fun observeCatalog(): Flow<Catalog>

    suspend fun createCatalogItem(
        name: String,
        unitPrice: BigDecimal,
        sectionTitle: String
    )

    suspend fun reorderCatalogItems(itemIdsInOrder: List<String>)
}
