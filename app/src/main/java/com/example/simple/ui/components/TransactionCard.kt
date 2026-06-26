package com.example.simple.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simple.domain.model.Transaction
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.domain.model.UserRole
import com.example.simple.ui.theme.SimpleColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

@Composable
fun TransactionCard(
    transaction: Transaction,
    userRole: UserRole? = null,
    onReturnClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = transaction.itemEmoji, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Qty: ${transaction.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Peminjam: ${transaction.userName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Asal: ${transaction.organizationName ?: "Unknown Org"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusBadge(status = transaction.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${dateFormatter.format(Date(transaction.borrowDate))} → " +
                        dateFormatter.format(Date(transaction.dueDate)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Only STAFF can validate returns
            if (userRole == UserRole.STAFF && (transaction.status == TransactionStatus.APPROVED || transaction.status == TransactionStatus.BORROWED) && onReturnClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onReturnClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SimpleColors.Success)
                ) {
                    Text("Validasi Pengembalian (Staff On-Duty)")
                }
            }
        }
    }
}
