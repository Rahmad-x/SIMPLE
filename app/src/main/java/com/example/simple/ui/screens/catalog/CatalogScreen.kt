package com.example.simple.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.ErrorScreen
import com.example.simple.ui.components.ItemCard
import com.example.simple.ui.components.LoadingScreen

@Composable
fun CatalogScreen(
    onItemClick: (String) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val catalogState by viewModel.catalogState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Katalog Barang",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearch,
                placeholder = { Text("Cari barang atau lokasi...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))

            when (val state = catalogState) {
                is CatalogState.Loading -> LoadingScreen()
                is CatalogState.Error -> ErrorScreen(message = state.message, onRetry = viewModel::refresh)
                is CatalogState.Success -> {
                    if (state.items.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Tidak ada barang ditemukan",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.items, key = { it.id }) { item ->
                                ItemCard(item = item, onClick = { onItemClick(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}