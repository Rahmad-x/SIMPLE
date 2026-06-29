package com.example.simple.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus

@Entity(tableName = "items")
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
    val status: String,
    val rentalPrice: Double,
    val isPaidRental: Boolean,
    val imageUrl: String?,
) {
    fun toDomain(): Item = Item(
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
        status = ItemStatus.fromString(status),
        rentalPrice = rentalPrice,
        isPaidRental = isPaidRental,
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(item: Item): ItemEntity = ItemEntity(
            id = item.id,
            organizationId = item.organizationId,
            name = item.name,
            description = item.description,
            category = item.category,
            location = item.location,
            totalStock = item.totalStock,
            availableStock = item.availableStock,
            condition = item.condition.name,
            emoji = item.emoji,
            status = item.status.name,
            rentalPrice = item.rentalPrice,
            isPaidRental = item.isPaidRental,
            imageUrl = item.imageUrl
        )
    }
}
