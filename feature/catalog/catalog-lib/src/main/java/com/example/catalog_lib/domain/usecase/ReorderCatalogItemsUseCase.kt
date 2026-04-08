package com.example.catalog_lib.domain.usecase

import com.example.catalog_lib.data.CatalogRoomDataSource
import javax.inject.Inject

class ReorderCatalogItemsUseCase @Inject constructor(
    private val repository: CatalogRoomDataSource
) {
    suspend operator fun invoke(itemIdsInOrder: List<String>) {
        if (itemIdsInOrder.isEmpty()) return
        repository.reorderCatalogItems(itemIdsInOrder)
    }
}
