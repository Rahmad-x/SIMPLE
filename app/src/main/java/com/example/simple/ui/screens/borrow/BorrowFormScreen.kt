package com.example.simple.ui.screens.borrow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.LoadingScreen
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.SimpleTextField
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowFormScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: BorrowFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val item by viewModel.item.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val requesterName by viewModel.requesterName.collectAsState()
    val requesterOrgName by viewModel.requesterOrgName.collectAsState()
    val totalFee by viewModel.totalFee.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateStartDate(it) }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateEndDate(it) }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    LaunchedEffect(state) {
        if (state is BorrowFormState.Success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form Peminjaman") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Item Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(text = item!!.emoji, style = MaterialTheme.typography.headlineLarge)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = item!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(text = "Tersedia: ${item!!.availableStock}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                        
                        Text(text = "Informasi Peminjam", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(text = requesterName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(text = "Organisasi: $requesterOrgName", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                SimpleTextField(
                    value = quantity,
                    onValueChange = viewModel::updateQuantity,
                    label = "Jumlah Barang",
                    placeholder = "1",
                    keyboardType = KeyboardType.Number
                )

                // Date Selection
                Column {
                    Text("Tanggal Peminjaman", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showStartDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(text = dateFormatter.format(Date(startDate)))
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    }
                }

                Column {
                    Text("Tanggal Pengembalian", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showEndDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(text = dateFormatter.format(Date(endDate)))
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    }
                }

                SimpleTextField(
                    value = notes,
                    onValueChange = viewModel::updateNotes,
                    label = "Keperluan / Catatan",
                    placeholder = "Contoh: Untuk kegiatan rapat BEM",
                    singleLine = false
                )

                if (item?.isPaidRental == true) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Estimasi Biaya Sewa", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "Rp ${String.format("%,.0f", totalFee)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "Rp ${String.format("%,.0f", item!!.rentalPrice)}/hari",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (state is BorrowFormState.Error) {
                    Text(
                        text = (state as BorrowFormState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                PrimaryButton(
                    text = "Ajukan Peminjaman",
                    onClick = viewModel::submit,
                    loading = state is BorrowFormState.Loading
                )
            }
        }
    }
}
