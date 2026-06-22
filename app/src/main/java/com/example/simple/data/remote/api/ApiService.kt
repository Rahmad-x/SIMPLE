package com.example.simple.data.remote.api

import com.example.simple.data.remote.dto.request.CreateItemRequest
import com.example.simple.data.remote.dto.request.CreateOrgRequest
import com.example.simple.data.remote.dto.request.JoinOrgRequest
import com.example.simple.data.remote.dto.request.LoginRequest
import com.example.simple.data.remote.dto.request.RejectRequestDto
import com.example.simple.data.remote.dto.request.SignUpRequest
import com.example.simple.data.remote.dto.request.SubmitBorrowRequestDto
import com.example.simple.data.remote.dto.request.UpdateItemRequest
import com.example.simple.data.remote.dto.response.BorrowRequestResponse
import com.example.simple.data.remote.dto.response.ItemResponse
import com.example.simple.data.remote.dto.response.LoginResponse
import com.example.simple.data.remote.dto.response.OrganizationResponse
import com.example.simple.data.remote.dto.response.TransactionResponse
import com.example.simple.data.remote.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Single Retrofit interface for all backend endpoints.
 * Split into separate *ApiService interfaces later if the backend grows large;
 * for now this matches ANDROID_API_CONTRACTS.md directly.
 */
interface ApiService {

    // ---------- Auth ----------
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignUpRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    // ---------- Organizations ----------
    @GET("organizations")
    suspend fun getOrganizations(): Response<List<OrganizationResponse>>

    @POST("organizations")
    suspend fun createOrganization(@Body request: CreateOrgRequest): Response<OrganizationResponse>

    @POST("organizations/join")
    suspend fun joinOrganization(@Body request: JoinOrgRequest): Response<OrganizationResponse>

    @PUT("users/me/active-organization/{id}")
    suspend fun switchOrganization(@Path("id") orgId: String): Response<Unit>

    // ---------- Items (Catalog) ----------
    @GET("organizations/{orgId}/items")
    suspend fun getItems(
        @Path("orgId") orgId: String,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
    ): Response<List<ItemResponse>>

    @GET("organizations/{orgId}/items/{itemId}")
    suspend fun getItemDetail(
        @Path("orgId") orgId: String,
        @Path("itemId") itemId: String,
    ): Response<ItemResponse>

    @POST("organizations/{orgId}/items")
    suspend fun createItem(
        @Path("orgId") orgId: String,
        @Body request: CreateItemRequest,
    ): Response<ItemResponse>

    @PATCH("organizations/{orgId}/items/{itemId}")
    suspend fun updateItem(
        @Path("orgId") orgId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemRequest,
    ): Response<ItemResponse>

    @DELETE("organizations/{orgId}/items/{itemId}")
    suspend fun deleteItem(
        @Path("orgId") orgId: String,
        @Path("itemId") itemId: String,
    ): Response<Unit>

    // ---------- Borrow & Transactions ----------
    @POST("organizations/{orgId}/borrow-requests")
    suspend fun submitBorrowRequest(
        @Path("orgId") orgId: String,
        @Body request: SubmitBorrowRequestDto,
    ): Response<BorrowRequestResponse>

    @GET("organizations/{orgId}/transactions")
    suspend fun getTransactions(
        @Path("orgId") orgId: String,
        @Query("status") status: String? = null,
    ): Response<List<TransactionResponse>>

    @POST("organizations/{orgId}/transactions/{txId}/return")
    suspend fun returnItem(
        @Path("orgId") orgId: String,
        @Path("txId") txId: String,
    ): Response<TransactionResponse>

    // ---------- Admin ----------
    @GET("organizations/{orgId}/requests")
    suspend fun getPendingRequests(@Path("orgId") orgId: String): Response<List<BorrowRequestResponse>>

    @POST("organizations/{orgId}/requests/{reqId}/approve")
    suspend fun approveBorrowRequest(
        @Path("orgId") orgId: String,
        @Path("reqId") reqId: String,
    ): Response<BorrowRequestResponse>

    @POST("organizations/{orgId}/requests/{reqId}/reject")
    suspend fun rejectBorrowRequest(
        @Path("orgId") orgId: String,
        @Path("reqId") reqId: String,
        @Body request: RejectRequestDto,
    ): Response<BorrowRequestResponse>

    @GET("organizations/{orgId}/members")
    suspend fun getMembers(@Path("orgId") orgId: String): Response<List<UserResponse>>
}