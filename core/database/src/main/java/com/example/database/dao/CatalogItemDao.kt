package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.model.CatalogItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogItemDao {
    @Query("SELECT * FROM catalog_items ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<CatalogItemEntity>>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM catalog_items")
    suspend fun nextSortOrder(): Int

    @Query("UPDATE catalog_items SET sortOrder = :sortOrder WHERE id = :itemId")
    suspend fun updateSortOrder(itemId: String, sortOrder: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CatalogItemEntity)
}
