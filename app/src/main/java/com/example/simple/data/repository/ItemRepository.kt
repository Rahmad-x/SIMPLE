package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.database.dao.ItemDao
import com.example.simple.data.mapper.toDomain
import com.example.simple.data.mapper.toEntity
import com.example.simple.data.remote.api.ApiService
import com.example.simple.domain.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val apiService: ApiService,
    private val itemDao: ItemDao,
) {
    /** Observe items from the local DB (source of truth), optionally filtered. */
    fun observeItems(orgId: String, search: String = "", category: String? = null): Flow<List<Item>> =
        itemDao.searchItems(orgId, search, category).map { entities -> entities.map { it.toDomain() } }

    /** Hits the network and refreshes the local cache. Call this on screen load / pull-to-refresh. */
    suspend fun refreshItems(orgId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getItems(orgId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                itemDao.upsertAll(body.map { it.toEntity() })
                Result.Success(Unit)
            } else {
                Result.Error("Gagal memuat katalog barang")
            }
        } catch (e: Exception) {
            // Network failed — local cache (if any) remains usable via observeItems().
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getItemDetail(orgId: String, itemId: String): Result<Item> = withContext(Dispatchers.IO) {
        try {
            val cached = itemDao.getItemById(itemId)
            val response = apiService.getItemDetail(orgId, itemId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                itemDao.upsertAll(listOf(body.toEntity()))
                Result.Success(body.toDomain())
            } else if (cached != null) {
                Result.Success(cached.toDomain())
            } else {
                Result.Error("Barang tidak ditemukan")
            }
        } catch (e: Exception) {
            val cached = itemDao.getItemById(itemId)
            if (cached != null) Result.Success(cached.toDomain())
            else Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }
}