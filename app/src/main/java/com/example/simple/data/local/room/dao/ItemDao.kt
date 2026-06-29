package com.example.simple.data.local.room.dao

import androidx.room.*
import com.example.simple.data.local.room.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE organizationId = :orgId")
    fun getItemsByOrgId(orgId: String): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Query("DELETE FROM items WHERE organizationId = :orgId")
    suspend fun deleteItemsByOrgId(orgId: String)
    
    @Transaction
    suspend fun syncItems(orgId: String, items: List<ItemEntity>) {
        deleteItemsByOrgId(orgId)
        insertItems(items)
    }
}
