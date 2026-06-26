package com.example.simple.domain.model

enum class NotificationType {
    BORROW_APPROVED,
    BORROW_REJECTED,
    DUE_REMINDER,
    OVERDUE_ALERT,
    SYSTEM;

    companion object {
        fun fromString(value: String): NotificationType = when (value.uppercase()) {
            "BORROW_APPROVED" -> BORROW_APPROVED
            "BORROW_REJECTED" -> BORROW_REJECTED
            "DUE_REMINDER" -> DUE_REMINDER
            "OVERDUE_ALERT" -> OVERDUE_ALERT
            else -> SYSTEM
        }
    }
}

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: Long,
    val isRead: Boolean = false,
    val organizationId: String? = null,
    val transactionId: String? = null,
)