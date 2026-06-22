package com.example.simple.data.mapper

import com.example.simple.data.local.database.entity.OrganizationEntity
import com.example.simple.data.local.database.entity.UserEntity
import com.example.simple.data.remote.dto.response.OrganizationResponse
import com.example.simple.data.remote.dto.response.UserResponse
import com.example.simple.domain.model.Organization
import com.example.simple.domain.model.User
import com.example.simple.domain.model.UserRole

fun OrganizationResponse.toEntity(): OrganizationEntity = OrganizationEntity(
    id = id,
    name = name,
    description = description,
    role = role,
    inviteCode = inviteCode,
    updatedAt = System.currentTimeMillis(),
)

fun OrganizationEntity.toDomain(): Organization = Organization(
    id = id,
    name = name,
    description = description,
    role = UserRole.fromString(role),
    inviteCode = inviteCode,
)

fun OrganizationResponse.toDomain(): Organization = Organization(
    id = id,
    name = name,
    description = description,
    role = UserRole.fromString(role),
    inviteCode = inviteCode,
)

fun UserResponse.toEntity(activeOrgId: String?): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatar = avatar,
    activeOrgId = activeOrgId ?: this.activeOrgId,
    updatedAt = System.currentTimeMillis(),
)

fun UserResponse.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatar = avatar,
    organizations = organizations.map { it.toDomain() },
    activeOrgId = activeOrgId,
)

fun UserEntity.toDomain(organizations: List<Organization> = emptyList()): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatar = avatar,
    organizations = organizations,
    activeOrgId = activeOrgId,
)