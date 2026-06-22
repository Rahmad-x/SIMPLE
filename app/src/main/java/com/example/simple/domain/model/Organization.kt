package com.example.simple.domain.model

data class Organization(
    val id: String,
    val name: String,
    val description: String? = null,
    val role: UserRole,
    val inviteCode: String? = null,
)