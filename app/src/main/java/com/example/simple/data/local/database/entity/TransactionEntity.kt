package com.example.simple.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("organizationId"), Index("userId"), Index("itemId")],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String?,
    val itemId: String,
    val itemName: String,
    val itemEmoji: String,
    val organizationId: String,
    val organizationName: String?,
    val quantity: Int,
    val borrowDate: Long,
    val dueDate: Long,
    val returnDate: Long?,
    val status: String,
    val notes: String?,
    val approvedBy: String?,
    val updatedAt: Long,
)