package com.example.simple.ui.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CatalogScreen(
    onItemClick: (String, String) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val catalogState by viewModel.catalogState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = viewModel::refresh
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Katalog Barang",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )

                Spacer(modifier = Modifier.padding(top = 8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearch,
                    placeholder = { Text("Cari barang atau lokasi...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.padding(top = 12.dp))

                when (val state = catalogState) {
                    is CatalogState.Loading -> {
                        // Only show initial loading screen if not pull-to-refreshing
                        if (searchQuery.isEmpty()) LoadingScreen()
                    }
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
                                    ItemCard(item = item, onClick = { onItemClick(item.organizationId, item.id) })
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
