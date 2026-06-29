package com.example.simple.domain.model

enum class ItemCondition {
    GOOD,
    FAIR,
    POOR;

    companion object {
        fun fromString(value: String): ItemCondition = when (value.lowercase()) {
            "good" -> GOOD
            "fair" -> FAIR
            else -> POOR
        }
    }
}

enum class ItemStatus {
    AVAILABLE,
    BORROWED,
    REPAIRING;

    companion object {
        fun fromString(value: String): ItemStatus = when (value.uppercase()) {
            "AVAILABLE" -> AVAILABLE
            "BORROWED" -> BORROWED
            "REPAIRING" -> REPAIRING
            else -> AVAILABLE
        }
    }
}

data class Item(
    val id: String,
    val organizationId: String,
    val name: String,
    val description: String? = null,
    val category: String,
    val location: String,
    val totalStock: Int,
    val availableStock: Int,
    val condition: ItemCondition,
    val emoji: String = "📦",
    val status: ItemStatus = ItemStatus.AVAILABLE,
    val rentalPrice: Double = 0.0,
    val isPaidRental: Boolean = false,
    val imageUrl: String? = null,
) {
    val isAvailable: Boolean get() = availableStock > 0 && status == ItemStatus.AVAILABLE
}