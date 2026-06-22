package com.example.simple.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simple.data.local.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId ORDER BY borrowDate DESC")
    fun observeByOrganization(orgId: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE organizationId = :orgId " +
                "AND (:status IS NULL OR status = :status) ORDER BY borrowDate DESC",
    )
    fun observeByStatus(orgId: String, status: String?): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' AND organizationId = :orgId ORDER BY borrowDate DESC")
    fun observePendingRequests(orgId: String): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET status = :status, returnDate = :returnDate WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, returnDate: Long?)

    @Query("DELETE FROM transactions WHERE organizationId = :orgId")
    suspend fun clearByOrganization(orgId: String)
}