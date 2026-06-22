package com.example.simple.data.remote.dto.request

data class SubmitBorrowRequestDto(
    val itemId: String,
    val quantity: Int,
    val startDate: Long,
    val endDate: Long,
    val notes: String? = null,
)

data class CreateItemRequest(
    val name: String,
    val description: String?,
    val category: String,
    val location: String,
    val totalStock: Int,
    val condition: String,
    val emoji: String?,
)

data class UpdateItemRequest(
    val name: String?,
    val description: String?,
    val category: String?,
    val location: String?,
    val totalStock: Int?,
    val condition: String?,
)

data class RejectRequestDto(
    val reason: String,
)