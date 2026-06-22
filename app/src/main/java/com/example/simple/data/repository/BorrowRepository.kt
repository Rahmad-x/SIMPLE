package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.database.dao.TransactionDao
import com.example.simple.data.mapper.toDomain
import com.example.simple.data.mapper.toEntity
import com.example.simple.data.remote.api.ApiService
import com.example.simple.data.remote.dto.request.SubmitBorrowRequestDto
import com.example.simple.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BorrowRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun submitBorrowRequest(
        orgId: String,
        itemId: String,
        quantity: Int,
        startDate: Long,
        endDate: Long,
        notes: String?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.submitBorrowRequest(
                orgId,
                SubmitBorrowRequestDto(itemId, quantity, startDate, endDate, notes),
            )
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Gagal mengajukan peminjaman")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }
}

@Singleton
class TransactionRepository @Inject constructor(
    private val apiService: ApiService,
    private val transactionDao: TransactionDao,
) {
    fun observeTransactions(orgId: String, status: String? = null): Flow<List<Transaction>> =
        transactionDao.observeByStatus(orgId, status).map { entities -> entities.map { it.toDomain() } }

    suspend fun refreshTransactions(orgId: String, status: String? = null): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTransactions(orgId, status)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    transactionDao.upsertAll(body.map { it.toEntity() })
                    Result.Success(Unit)
                } else {
                    Result.Error("Gagal memuat riwayat transaksi")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Tidak dapat terhubung ke server")
            }
        }

    suspend fun returnItem(orgId: String, transactionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.returnItem(orgId, transactionId)
            if (response.isSuccessful) {
                transactionDao.updateStatus(transactionId, "COMPLETED", System.currentTimeMillis())
                Result.Success(Unit)
            } else {
                Result.Error("Gagal mencatat pengembalian")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }
}