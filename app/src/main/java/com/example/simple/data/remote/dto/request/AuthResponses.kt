package com.example.simple.data.remote.dto.response

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar: String?,
    val organizations: List<OrganizationResponse> = emptyList(),
    val activeOrgId: String? = null,
)

data class OrganizationResponse(
    val id: String,
    val name: String,
    val description: String?,
    val role: String,
    val inviteCode: String?,
)

data class LoginResponse(
    val token: String,
    val user: UserResponse,
)