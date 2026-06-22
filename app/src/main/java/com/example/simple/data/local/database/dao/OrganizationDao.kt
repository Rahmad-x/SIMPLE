package com.example.simple.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simple.data.local.database.entity.OrganizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(organizations: List<OrganizationEntity>)

    @Query("SELECT * FROM organizations")
    fun observeAll(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    suspend fun getById(orgId: String): OrganizationEntity?

    @Query("DELETE FROM organizations")
    suspend fun clearAll()
}