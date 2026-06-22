package com.example.simple.data.remote.dto.request

data class LoginRequest(
    val email: String,
    val password: String,
)

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
)

data class CreateOrgRequest(
    val name: String,
    val description: String? = null,
)

data class JoinOrgRequest(
    val inviteCode: String,
)