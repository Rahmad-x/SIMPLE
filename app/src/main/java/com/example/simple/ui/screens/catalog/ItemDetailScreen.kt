package com.example.simple.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.ErrorScreen
import com.example.simple.ui.components.LoadingScreen
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.StatusBadge
import com.example.simple.domain.model.TransactionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBorrowForm: (String, String) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Barang") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when (val itemState = state) {
            is ItemDetailState.Loading -> LoadingScreen()
            is ItemDetailState.Error -> ErrorScreen(message = itemState.message, onRetry = viewModel::loadItem)
            is ItemDetailState.Success -> {
                val item = itemState.item
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header with Emoji
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 80.sp)
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Reusing StatusBadge but it expects TransactionStatus, 
                            // let's just show availability
                            val availabilityText = if (item.isAvailable) "Tersedia" else "Penuh"
                            val availabilityColor = if (item.isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336)
                            
                            Surface(
                                color = availabilityColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = availabilityText,
                                    color = availabilityColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = item.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.description ?: "Tidak ada deskripsi untuk barang ini.",
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Stok Tersedia", style = MaterialTheme.typography.labelMedium)
                                    Text("${item.availableStock} / ${item.totalStock}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                if (item.isPaidRental) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Biaya Sewa", style = MaterialTheme.typography.labelMedium)
                                        Text("Rp ${item.rentalPrice.toInt()}/hari", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        PrimaryButton(
                            text = if (item.isAvailable) "Pinjam Sekarang" else "Tidak Tersedia",
                            onClick = {
                                onNavigateToBorrowForm(item.organizationId, item.id)
                            },
                            enabled = item.isAvailable
                        )
                    }
                }
            }
        }
    }
}
