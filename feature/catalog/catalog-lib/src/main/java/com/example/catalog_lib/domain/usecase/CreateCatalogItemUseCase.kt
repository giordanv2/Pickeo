package com.example.catalog_lib.domain.usecase

import com.example.catalog_lib.data.CatalogRepository
import java.math.BigDecimal
import javax.inject.Inject

class CreateCatalogItemUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(
        name: String,
        unitPrice: BigDecimal,
        sectionTitle: String
    ) {
        repository.createCatalogItem(
            name = name,
            unitPrice = unitPrice,
            sectionTitle = sectionTitle
        )
    }
}
