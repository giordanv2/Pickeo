package com.example.catalog_feat.presentation

import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import java.math.BigDecimal

object CatalogFixtures {
    fun sampleCatalog(): Catalog {
        val coffeeSection = CatalogSection(
            id = "coffee",
            title = "Coffee",
            items = listOf(
                CatalogItem(id = "espresso", name = "Espresso", price = BigDecimal("2.50")),
                CatalogItem(id = "americano", name = "Americano", price = BigDecimal("3.00")),
                CatalogItem(id = "latte", name = "Caffe Latte", price = BigDecimal("4.25"))
            ),
            gridColumns = 3
        )

        val bakerySection = CatalogSection(
            id = "bakery",
            title = "Bakery",
            items = listOf(
                CatalogItem(id = "croissant", name = "Butter Croissant", price = BigDecimal("3.50")),
                CatalogItem(id = "muffin", name = "Blueberry Muffin", price = BigDecimal("3.25")),
                CatalogItem(
                    id = "banana-bread",
                    name = "Banana Bread",
                    price = BigDecimal("3.75"),
                    isAvailable = false
                )
            ),
            gridColumns = 3
        )

        return Catalog(
            id = "main-menu",
            name = "Main Catalog",
            sections = listOf(coffeeSection, bakerySection)
        )
    }
}
