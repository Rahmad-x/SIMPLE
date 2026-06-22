package com.example.simple.data.mapper

import com.example.simple.data.local.database.entity.ItemEntity
import com.example.simple.data.remote.dto.response.ItemResponse
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition

fun ItemResponse.toEntity(): ItemEntity = ItemEntity(
    id = id,
    organizationId = organizationId,
    name = name,
    description = description,
    category = category,
    location = location,
    totalStock = totalStock,
    availableStock = availableStock,
    condition = condition,
    emoji = emoji ?: "📦",
    updatedAt = System.currentTimeMillis(),
)

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    organizationId = organizationId,
    name = name,
    description = description,
    category = category,
    location = location,
    totalStock = totalStock,
    availableStock = availableStock,
    condition = ItemCondition.fromString(condition),
    emoji = emoji,
)

fun ItemResponse.toDomain(): Item = Item(
    id = id,
    organizationId = organizationId,
    name = name,
    description = description,
    category = category,
    location = location,
    totalStock = totalStock,
    availableStock = availableStock,
    condition = ItemCondition.fromString(condition),
    emoji = emoji ?: "📦",
)