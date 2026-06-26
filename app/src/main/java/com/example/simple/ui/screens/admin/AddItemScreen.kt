package com.example.simple.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.SimpleTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val category by viewModel.category.collectAsState()
    val location by viewModel.location.collectAsState()
    val totalStock by viewModel.totalStock.collectAsState()
    val emoji by viewModel.emoji.collectAsState()

    LaunchedEffect(state) {
        if (state is AddItemState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Barang Baru") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SimpleTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = "Nama Barang",
                placeholder = "Contoh: Proyektor Epson"
            )

            SimpleTextField(
                value = emoji,
                onValueChange = viewModel::updateEmoji,
                label = "Emoji Ikon",
                placeholder = "📦"
            )

            SimpleTextField(
                value = category,
                onValueChange = viewModel::updateCategory,
                label = "Kategori",
                placeholder = "Contoh: Elektronik"
            )

            SimpleTextField(
                value = location,
                onValueChange = viewModel::updateLocation,
                label = "Lokasi",
                placeholder = "Contoh: Lemari A1"
            )

            SimpleTextField(
                value = totalStock,
                onValueChange = viewModel::updateTotalStock,
                label = "Total Stok",
                placeholder = "1",
                keyboardType = KeyboardType.Number
            )

            SimpleTextField(
                value = description,
                onValueChange = viewModel::updateDescription,
                label = "Deskripsi (Opsional)",
                placeholder = "Detail barang..."
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state is AddItemState.Error) {
                Text(
                    text = (state as AddItemState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryButton(
                text = "Simpan Barang",
                onClick = viewModel::submit,
                loading = state is AddItemState.Loading
            )
        }
    }
}
