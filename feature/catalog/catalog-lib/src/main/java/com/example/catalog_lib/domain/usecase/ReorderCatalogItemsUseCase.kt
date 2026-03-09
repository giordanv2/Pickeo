package com.example.catalog_lib.domain.usecase

import com.example.catalog_lib.data.CatalogRepository
import javax.inject.Inject

class ReorderCatalogItemsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(itemIdsInOrder: List<String>) {
        if (itemIdsInOrder.isEmpty()) return
        repository.reorderCatalogItems(itemIdsInOrder)
    }
}
