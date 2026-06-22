package com.example.simple.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val role: String,
    val inviteCode: String?,
    val updatedAt: Long,
)