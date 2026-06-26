package com.example.simple.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.ui.theme.SimpleColors

@Composable
fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (status) {
        TransactionStatus.PENDING -> Triple(SimpleColors.PendingContainer, SimpleColors.Pending, "Menunggu")
        TransactionStatus.APPROVED -> Triple(SimpleColors.ActiveContainer, SimpleColors.Active, "Disetujui")
        TransactionStatus.BORROWED -> Triple(SimpleColors.ActiveContainer, SimpleColors.Active, "Dipinjam")
        TransactionStatus.RETURNED -> Triple(SimpleColors.SuccessContainer, SimpleColors.Success, "Selesai")
        TransactionStatus.OVERDUE -> Triple(SimpleColors.OverdueContainer, SimpleColors.Overdue, "Terlambat")
        TransactionStatus.REJECTED -> Triple(SimpleColors.ErrorContainer, SimpleColors.Error, "Ditolak")
    }

    Text(
        text = label,
        color = fg,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}