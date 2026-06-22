package com.example.simple.data.remote.dto.response

data class ItemResponse(
    val id: String,
    val organizationId: String,
    val name: String,
    val description: String?,
    val category: String,
    val location: String,
    val totalStock: Int,
    val availableStock: Int,
    val condition: String,
    val emoji: String?,
)

data class TransactionResponse(
    val id: String,
    val userId: String,
    val userName: String?,
    val itemId: String,
    val itemName: String,
    val itemEmoji: String?,
    val organizationId: String,
    val organizationName: String?,
    val quantity: Int,
    val borrowDate: Long,
    val dueDate: Long,
    val returnDate: Long?,
    val status: String,
    val notes: String?,
    val approvedBy: String?,
)

data class BorrowRequestResponse(
    val id: String,
    val itemId: String,
    val itemName: String,
    val requesterId: String,
    val requesterName: String,
    val organizationId: String,
    val quantity: Int,
    val startDate: Long,
    val endDate: Long,
    val notes: String?,
    val status: String,
)