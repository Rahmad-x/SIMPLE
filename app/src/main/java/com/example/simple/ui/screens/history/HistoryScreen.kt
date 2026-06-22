package com.example.simple.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.ui.components.ErrorScreen
import com.example.simple.ui.components.LoadingScreen
import com.example.simple.ui.components.TransactionCard

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyState by viewModel.historyState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val filters = listOf<Pair<String, TransactionStatus?>>(
        "Semua" to null,
        "Aktif" to TransactionStatus.ACTIVE,
        "Selesai" to TransactionStatus.COMPLETED,
        "Ditolak" to TransactionStatus.REJECTED,
        "Terlambat" to TransactionStatus.OVERDUE,
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Riwayat Transaksi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { (label, status) ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { viewModel.setFilter(status) },
                        label = { Text(label) },
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))

            when (val state = historyState) {
                is HistoryState.Loading -> LoadingScreen()
                is HistoryState.Error -> ErrorScreen(message = state.message, onRetry = {})
                is HistoryState.Success -> {
                    if (state.transactions.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Belum ada transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.transactions, key = { it.id }) { tx ->
                                TransactionCard(
                                    transaction = tx,
                                    onReturnClick = { viewModel.returnItem(tx.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}