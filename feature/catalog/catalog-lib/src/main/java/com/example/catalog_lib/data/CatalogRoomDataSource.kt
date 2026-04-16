package com.example.catalog_lib.data

import com.example.catalog_lib.models.Catalog
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface CatalogRoomDataSource {
    fun observeCatalog(): Flow<Catalog>

    suspend fun createCatalogItem(
        name: String,
        unitPrice: BigDecimal,
        sectionTitle: String,
        imageUrl: String? = null,
    )

    suspend fun reorderCatalogItems(itemIdsInOrder: List<String>)

    suspend fun removeCatalogItem(itemId: String)
}
