package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.data.mapper.toDomain
import com.example.simple.data.remote.api.ApiService
import com.example.simple.data.remote.dto.request.LoginRequest
import com.example.simple.data.remote.dto.request.SignUpRequest
import com.example.simple.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
) {
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                sessionManager.saveSession(body.token, body.user.id)
                body.user.activeOrgId?.let { sessionManager.setActiveOrganization(it) }
                Result.Success(body.user.toDomain())
            } else {
                Result.Error(mapHttpError(response.code()))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun signup(name: String, email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.signup(SignUpRequest(name, email, password))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    sessionManager.saveSession(body.token, body.user.id)
                    Result.Success(body.user.toDomain())
                } else {
                    Result.Error(mapHttpError(response.code()))
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Tidak dapat terhubung ke server")
            }
        }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
            sessionManager.clearSession()
            Result.Success(Unit)
        } catch (e: Exception) {
            // Even if the network call fails, clear local session so the user isn't stuck.
            sessionManager.clearSession()
            Result.Success(Unit)
        }
    }

    suspend fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    private fun mapHttpError(code: Int): String = when (code) {
        401 -> "Email atau password salah"
        409 -> "Email sudah terdaftar"
        else -> "Terjadi kesalahan, coba lagi"
    }
}