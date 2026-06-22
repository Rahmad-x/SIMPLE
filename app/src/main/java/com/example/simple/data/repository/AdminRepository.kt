package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.mapper.toDomain
import com.example.simple.data.mapper.toEntity
import com.example.simple.data.remote.api.ApiService
import com.example.simple.data.remote.dto.request.CreateItemRequest
import com.example.simple.data.remote.dto.request.RejectRequestDto
import com.example.simple.data.remote.dto.request.UpdateItemRequest
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.domain.model.User
import com.example.simple.data.local.database.dao.ItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService,
    private val itemDao: ItemDao,
) {
    suspend fun getPendingRequests(orgId: String): Result<List<BorrowRequest>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPendingRequests(orgId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(
                    body.map {
                        BorrowRequest(
                            id = it.id,
                            itemId = it.itemId,
                            itemName = it.itemName,
                            requesterId = it.requesterId,
                            requesterName = it.requesterName,
                            organizationId = it.organizationId,
                            quantity = it.quantity,
                            startDate = it.startDate,
                            endDate = it.endDate,
                            notes = it.notes,
                            status = TransactionStatus.fromString(it.status),
                        )
                    },
                )
            } else {
                Result.Error("Gagal memuat permintaan pending")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun approveRequest(orgId: String, requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveBorrowRequest(orgId, requestId)
            if (response.isSuccessful) Result.Success(Unit) else Result.Error("Gagal menyetujui permintaan")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun rejectRequest(orgId: String, requestId: String, reason: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.rejectBorrowRequest(orgId, requestId, RejectRequestDto(reason))
                if (response.isSuccessful) Result.Success(Unit) else Result.Error("Gagal menolak permintaan")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Tidak dapat terhubung ke server")
            }
        }

    suspend fun addItem(orgId: String, request: CreateItemRequest): Result<Item> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createItem(orgId, request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                itemDao.upsertAll(listOf(body.toEntity()))
                Result.Success(body.toDomain())
            } else {
                Result.Error("Gagal menambah barang")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun updateItem(orgId: String, itemId: String, request: UpdateItemRequest): Result<Item> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateItem(orgId, itemId, request)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    itemDao.upsertAll(listOf(body.toEntity()))
                    Result.Success(body.toDomain())
                } else {
                    Result.Error("Gagal memperbarui barang")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Tidak dapat terhubung ke server")
            }
        }

    suspend fun deleteItem(orgId: String, itemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteItem(orgId, itemId)
            if (response.isSuccessful) Result.Success(Unit) else Result.Error("Gagal menghapus barang")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getMembers(orgId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMembers(orgId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body.map { it.toDomain() })
            } else {
                Result.Error("Gagal memuat anggota")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }
}