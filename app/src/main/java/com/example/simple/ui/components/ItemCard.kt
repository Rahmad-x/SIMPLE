package com.example.simple.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simple.domain.model.Item
import com.example.simple.ui.theme.SimpleColors

@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(text = item.emoji, style = MaterialTheme.typography.headlineMedium)

            Column(
                modifier = Modifier.padding(start = 12.dp).weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${item.availableStock}/${item.totalStock} tersedia",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.isAvailable) SimpleColors.Success else SimpleColors.Error,
                )
                
                if (item.isPaidRental) {
                    Text(
                        text = "Rp ${String.format("%,.0f", item.rentalPrice)}/hari",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AvailabilityDot(isAvailable = item.isAvailable)
        }
    }
}

@Composable
private fun AvailabilityDot(isAvailable: Boolean) {
    val color = if (isAvailable) SimpleColors.Success else SimpleColors.Error
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .padding(4.dp)
            .background(color, RoundedCornerShape(50))
            .padding(6.dp),
    )
}