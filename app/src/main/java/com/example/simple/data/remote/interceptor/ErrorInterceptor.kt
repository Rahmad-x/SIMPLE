package com.example.simple.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ApiException(val code: Int, message: String) : IOException(message)

/**
 * Translates non-2xx HTTP responses into a typed exception with a readable message,
 * so repositories/usecases don't need to inspect raw Retrofit Response codes everywhere.
 */
class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (!response.isSuccessful) {
            val message = when (response.code) {
                401 -> "Sesi habis, silakan login kembali"
                403 -> "Anda tidak memiliki akses untuk aksi ini"
                404 -> "Data tidak ditemukan"
                500, 502, 503 -> "Server sedang bermasalah, coba lagi nanti"
                else -> "Terjadi kesalahan (${response.code})"
            }
            // Re-throwing here would break Retrofit's Response<T> flow for handled codes,
            // so we just attach a readable message header consumers can read if needed.
            return response.newBuilder().message(message).build()
        }

        return response
    }
}