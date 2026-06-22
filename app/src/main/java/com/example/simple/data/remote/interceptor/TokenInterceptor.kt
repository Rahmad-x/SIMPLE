package com.example.simple.data.remote.interceptor

import com.example.simple.data.local.preferences.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull // <-- Tambahkan ini jika sessionManager.tokenFlow menggunakan Flow
import kotlin.text.isNullOrBlank          // <-- Tambahkan ini untuk fungsi pengecekan string kosong

@Singleton
class TokenInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Mengambil token secara sinkronus dari SessionManager menggunakan runBlocking
        val token = runBlocking {
            sessionManager.tokenFlow.firstOrNull() // Sesuaikan nama properti flow token di SessionManager kamu
        }

        // Jika token ditemukan (user sudah login), suntikkan ke header Authorization
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}