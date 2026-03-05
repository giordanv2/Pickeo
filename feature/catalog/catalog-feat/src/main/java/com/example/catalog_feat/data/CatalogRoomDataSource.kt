package com.example.catalog_feat.data

import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import com.example.catalog_lib.data.CatalogRepository
import com.example.database.dao.CatalogItemDao
import com.example.database.model.CatalogItemEntity
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CatalogRoomDataSource @Inject constructor(
    private val catalogItemDao: CatalogItemDao
) : CatalogRepository {

    override fun observeCatalog(): Flow<Catalog> {
        return catalogItemDao.observeAll().map { entities -> entities.toCatalog() }
    }

    override suspend fun createCatalogItem(
        name: String,
        unitPrice: BigDecimal,
        sectionTitle: String
    ) {
        val entity = CatalogItemEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            unitPrice = unitPrice.toPlainString(),
            sectionId = sectionTitle.toSectionId(),
            sectionTitle = sectionTitle,
            isAvailable = true,
            createdAt = System.currentTimeMillis()
        )
        catalogItemDao.upsert(entity)
    }
}

private fun List<CatalogItemEntity>.toCatalog(): Catalog {
    val sections = this
        .groupBy { it.sectionId }
        .mapNotNull { (_, entities) ->
            val first = entities.firstOrNull() ?: return@mapNotNull null
            CatalogSection(
                id = first.sectionId,
                title = first.sectionTitle,
                items = entities.map { entity ->
                    CatalogItem(
                        id = entity.id,
                        name = entity.name,
                        price = entity.unitPrice.toBigDecimal(),
                        isAvailable = entity.isAvailable
                    )
                }
            )
        }

    return Catalog(
        id = "main-catalog",
        name = "Catalog",
        sections = sections
    )
}

private fun String.toSectionId(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifEmpty { "general" }
}
