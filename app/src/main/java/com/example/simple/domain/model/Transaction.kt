package com.example.simple.domain.model

enum class TransactionStatus {
    PENDING,
    APPROVED,
    BORROWED,
    OVERDUE,
    RETURNED,
    REJECTED;

    companion object {
        fun fromString(value: String): TransactionStatus = when (value.uppercase()) {
            "PENDING" -> PENDING
            "APPROVED" -> APPROVED
            "BORROWED" -> BORROWED
            "OVERDUE" -> OVERDUE
            "RETURNED" -> RETURNED
            "REJECTED" -> REJECTED
            else -> PENDING
        }
    }
}

data class Transaction(
    val id: String,
    val userId: String,
    val userName: String? = null,
    val itemId: String,
    val itemName: String,
    val itemEmoji: String = "📦",
    val organizationId: String,
    val organizationName: String? = null,
    val quantity: Int,
    val borrowDate: Long,
    val dueDate: Long,
    val returnDate: Long? = null,
    val status: TransactionStatus,
    val notes: String? = null,
    val approvedBy: String? = null,
    val conditionBefore: ItemCondition = ItemCondition.GOOD,
    val conditionAfter: ItemCondition? = null,
    val totalFee: Double = 0.0,
    val lateFee: Double = 0.0,
)

data class BorrowRequest(
    val id: String,
    val itemId: String,
    val itemName: String,
    val requesterId: String,
    val requesterName: String,
    val organizationId: String,
    val quantity: Int,
    val startDate: Long,
    val endDate: Long,
    val notes: String? = null,
    val status: TransactionStatus,
    val totalFee: Double = 0.0,
)