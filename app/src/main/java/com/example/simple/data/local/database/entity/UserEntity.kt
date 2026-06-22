package com.example.simple.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar: String?,
    val activeOrgId: String?,
    val updatedAt: Long,
)