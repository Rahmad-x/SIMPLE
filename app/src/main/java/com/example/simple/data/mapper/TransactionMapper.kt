package com.example.simple.data.mapper

import com.example.simple.data.local.database.entity.TransactionEntity
import com.example.simple.data.remote.dto.response.TransactionResponse
import com.example.simple.domain.model.Transaction
import com.example.simple.domain.model.TransactionStatus

fun TransactionResponse.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    userId = userId,
    userName = userName,
    itemId = itemId,
    itemName = itemName,
    itemEmoji = itemEmoji ?: "📦",
    organizationId = organizationId,
    organizationName = organizationName,
    quantity = quantity,
    borrowDate = borrowDate,
    dueDate = dueDate,
    returnDate = returnDate,
    status = status,
    notes = notes,
    approvedBy = approvedBy,
    updatedAt = System.currentTimeMillis(),
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    userId = userId,
    userName = userName,
    itemId = itemId,
    itemName = itemName,
    itemEmoji = itemEmoji,
    organizationId = organizationId,
    organizationName = organizationName,
    quantity = quantity,
    borrowDate = borrowDate,
    dueDate = dueDate,
    returnDate = returnDate,
    status = TransactionStatus.fromString(status),
    notes = notes,
    approvedBy = approvedBy,
)

fun TransactionResponse.toDomain(): Transaction = Transaction(
    id = id,
    userId = userId,
    userName = userName,
    itemId = itemId,
    itemName = itemName,
    itemEmoji = itemEmoji ?: "📦",
    organizationId = organizationId,
    organizationName = organizationName,
    quantity = quantity,
    borrowDate = borrowDate,
    dueDate = dueDate,
    returnDate = returnDate,
    status = TransactionStatus.fromString(status),
    notes = notes,
    approvedBy = approvedBy,
)