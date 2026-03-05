package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.model.CatalogItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogItemDao {
    @Query("SELECT * FROM catalog_items ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<CatalogItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CatalogItemEntity)
}
