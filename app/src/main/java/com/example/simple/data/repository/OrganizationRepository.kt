package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.database.dao.OrganizationDao
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.data.mapper.toDomain
import com.example.simple.data.mapper.toEntity
import com.example.simple.data.remote.api.ApiService
import com.example.simple.data.remote.dto.request.CreateOrgRequest
import com.example.simple.data.remote.dto.request.JoinOrgRequest
import com.example.simple.domain.model.Organization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganizationRepository @Inject constructor(
    private val apiService: ApiService,
    private val organizationDao: OrganizationDao,
    private val sessionManager: SessionManager,
) {
    fun observeOrganizations(): Flow<List<Organization>> =
        organizationDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun refreshOrganizations(): Result<List<Organization>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOrganizations()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                organizationDao.upsertAll(body.map { it.toEntity() })
                Result.Success(body.map { it.toDomain() })
            } else {
                Result.Error("Gagal memuat daftar organisasi")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun createOrganization(name: String, description: String?): Result<Organization> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createOrganization(CreateOrgRequest(name, description))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    organizationDao.upsertAll(listOf(body.toEntity()))
                    sessionManager.setActiveOrganization(body.id)
                    Result.Success(body.toDomain())
                } else {
                    Result.Error("Gagal membuat organisasi")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Tidak dapat terhubung ke server")
            }
        }

    suspend fun joinOrganization(inviteCode: String): Result<Organization> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.joinOrganization(JoinOrgRequest(inviteCode))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                organizationDao.upsertAll(listOf(body.toEntity()))
                sessionManager.setActiveOrganization(body.id)
                Result.Success(body.toDomain())
            } else {
                Result.Error("Kode undangan tidak valid")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun switchOrganization(orgId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sessionManager.setActiveOrganization(orgId)
            apiService.switchOrganization(orgId)
            Result.Success(Unit)
        } catch (e: Exception) {
            // Local switch already succeeded; backend sync failure shouldn't block the UI.
            Result.Success(Unit)
        }
    }

    val activeOrgIdFlow: Flow<String?> = sessionManager.activeOrgIdFlow
}