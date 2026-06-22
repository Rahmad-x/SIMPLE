package com.example.simple.domain.model

enum class UserRole {
    ADMIN,
    STAFF,
    BORROWER;

    companion object {
        fun fromString(value: String): UserRole = when (value.uppercase()) {
            "ADMIN" -> ADMIN
            "STAFF" -> STAFF
            else -> BORROWER
        }
    }
}