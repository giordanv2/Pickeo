package com.example.catalog_lib.domain.usecase

import com.example.catalog_lib.data.CatalogRoomDataSource
import javax.inject.Inject

class RemoveCatalogItemUseCase @Inject constructor(
    private val repository: CatalogRoomDataSource
) {
    suspend operator fun invoke(itemId: String) {
        repository.removeCatalogItem(itemId)
    }
}
