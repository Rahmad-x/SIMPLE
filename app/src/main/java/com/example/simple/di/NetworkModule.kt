package com.example.simple.di

import com.example.simple.data.remote.api.ApiService
import com.example.simple.data.remote.interceptor.ErrorInterceptor
import com.example.simple.data.remote.interceptor.TokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 1. Ganti URL ini dengan domain live hosting tempat backend Anda ditaruh (misal: Railway, Render, VPS)
    // Sesuai Aturan PRD Keamanan, wajib menggunakan protokol HTTPS aman
    private const val MAIN_BACKEND_URL = "https://api-simple.rahmad.com/v1/"

    // 2. Base URL untuk integrasi eksternal sesuai Bab 10 PRD Anda
    private const val FAKE_STORE_URL = "https://fakestoreapi.com/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun provideErrorInterceptor(): ErrorInterceptor = ErrorInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenInterceptor: TokenInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor) // Menyuntikkan Token + Active Organization Context per-request
        .addInterceptor(errorInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // RETROFIT INSTANCE 1: Main Backend Aplikasi (Multi-Tenant & Security Terisolasi)
    @Provides
    @Singleton
    @Named("MainRetrofit")
    fun provideMainRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(MAIN_BACKEND_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // RETROFIT INSTANCE 2: Eksternal Fake Store API (Asynchronous Fetch Tanpa Interceptor Token)
    @Provides
    @Singleton
    @Named("FakeStoreRetrofit")
    fun provideFakeStoreRetrofit(loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        val publicClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(FAKE_STORE_URL)
            .client(publicClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // PROVIDE API SERVICE UTAMA
    @Provides
    @Singleton
    fun provideApiService(@Named("MainRetrofit") retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // PROVIDE API SERVICE FAKE STORE (Tambahkan interface ini nanti jika Anda memisahkan fungsinya)
    /*
    @Provides
    @Singleton
    fun provideFakeStoreApiService(@Named("FakeStoreRetrofit") retrofit: Retrofit): FakeStoreApiService =
        retrofit.create(FakeStoreApiService::class.java)
    */
}