package com.example.simple.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgbbApiService {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): ImgbbResponse
}

data class ImgbbResponse(
    val data: ImgbbData?,
    val success: Boolean,
    val status: Int
)

data class ImgbbData(
    val url: String,
    val display_url: String,
    val delete_url: String
)
