package com.example.simple.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
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
    val isPaidRental by viewModel.isPaidRental.collectAsState()
    val rentalPrice by viewModel.rentalPrice.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    
    val isEditing = viewModel.isEditing

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        viewModel.updateImageUri(uri)
    }

    LaunchedEffect(state) {
        if (state is AddItemState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Barang" else "Tambah Barang Baru") },
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
            // Photo Picker Section
            Card(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(48.dp))
                            Text("Tambah Foto Barang", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Barang Sewa Berbayar", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isPaidRental, onCheckedChange = viewModel::updateIsPaidRental)
            }

            if (isPaidRental) {
                SimpleTextField(
                    value = rentalPrice,
                    onValueChange = viewModel::updateRentalPrice,
                    label = "Harga Sewa per Hari",
                    placeholder = "0.0",
                    keyboardType = KeyboardType.Decimal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state is AddItemState.Error) {
                Text(
                    text = (state as AddItemState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryButton(
                text = if (isEditing) "Simpan Perubahan" else "Simpan Barang",
                onClick = viewModel::submit,
                loading = state is AddItemState.Loading
            )
        }
    }
}
