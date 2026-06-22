package com.example.simple.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = OrganizationEntity::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("organizationId")],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String,
    val description: String?,
    val category: String,
    val location: String,
    val totalStock: Int,
    val availableStock: Int,
    val condition: String,
    val emoji: String,
    val updatedAt: Long,
)