package com.example.simple.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simple.data.local.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ItemEntity>)

    @Query("SELECT * FROM items WHERE organizationId = :orgId ORDER BY name ASC")
    fun observeItemsByOrganization(orgId: String): Flow<List<ItemEntity>>

    @Query(
        "SELECT * FROM items WHERE organizationId = :orgId " +
                "AND (:category IS NULL OR category = :category) " +
                "AND (name LIKE '%' || :search || '%' OR location LIKE '%' || :search || '%') " +
                "ORDER BY name ASC",
    )
    fun searchItems(orgId: String, search: String, category: String?): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): ItemEntity?

    @Query("DELETE FROM items WHERE organizationId = :orgId")
    suspend fun clearByOrganization(orgId: String)
}