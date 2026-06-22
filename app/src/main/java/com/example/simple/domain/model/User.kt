package com.example.simple.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val avatar: String? = null,
    val organizations: List<Organization> = emptyList(),
    val activeOrgId: String? = null,
)