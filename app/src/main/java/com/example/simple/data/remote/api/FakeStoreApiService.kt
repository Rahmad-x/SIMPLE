package com.example.simple.data.remote.api

import com.example.simple.data.remote.model.FakeProduct
import retrofit2.http.GET

interface FakeStoreApiService {
    @GET("products")
    suspend fun getProducts(): List<FakeProduct>
}
