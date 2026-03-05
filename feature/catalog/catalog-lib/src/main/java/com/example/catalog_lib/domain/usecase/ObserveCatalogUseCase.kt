package com.example.catalog_lib.domain.usecase

import com.example.catalog_lib.data.CatalogRepository
import com.example.catalog_lib.models.Catalog
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCatalogUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    operator fun invoke(): Flow<Catalog> = repository.observeCatalog()
}
