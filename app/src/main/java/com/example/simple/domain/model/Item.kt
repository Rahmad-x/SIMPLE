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
) {
    val isAvailable: Boolean get() = availableStock > 0
}