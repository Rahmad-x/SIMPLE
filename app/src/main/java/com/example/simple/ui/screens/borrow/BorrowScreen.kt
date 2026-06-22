package com.example.simple.ui.screens.borrow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.ItemCard
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.SimpleTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

@Composable
fun BorrowScreen(
    onRequestSubmitted: () -> Unit,
    viewModel: BorrowViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        if (submitState is BorrowSubmitState.Success) onRequestSubmitted()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (selectedItem == null) {
                Text(
                    text = "Pilih Barang",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Step 1: Pilih barang yang ingin dipinjam",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (items.isEmpty()) {
                    Text("Tidak ada barang yang tersedia saat ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items, key = { it.id }) { item ->
                            ItemCard(item = item, onClick = { viewModel.selectItem(item) })
                        }
                    }
                }
            } else {
                val item = selectedItem!!
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = viewModel::resetSelection) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                    Column {
                        Text(
                            text = "Detail Peminjaman",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Step 2: Isi detail peminjaman",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Column {
                            Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${item.availableStock} tersedia di ${item.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Jumlah", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { viewModel.updateQuantity(quantity - 1) }) { Text("−") }
                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    OutlinedButton(onClick = { viewModel.updateQuantity(quantity + 1) }) { Text("+") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tanggal Pinjam: ${dateFormatter.format(Date(startDate))}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Tanggal Kembali: ${dateFormatter.format(Date(endDate))}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "(Atur tanggal lewat date picker — sambungkan DatePickerDialog di sini)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                SimpleTextField(
                    value = notes,
                    onValueChange = viewModel::updateNotes,
                    label = "Catatan (opsional)",
                    placeholder = "Contoh: untuk acara seminar tanggal 25",
                )

                if (submitState is BorrowSubmitState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (submitState as BorrowSubmitState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryButton(
                    text = "Ajukan Peminjaman",
                    onClick = viewModel::submit,
                    loading = submitState is BorrowSubmitState.Loading,
                )
            }
        }
    }
}