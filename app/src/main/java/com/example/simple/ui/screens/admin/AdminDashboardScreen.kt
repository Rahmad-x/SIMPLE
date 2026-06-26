package com.example.simple.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.model.UserRole
import com.example.simple.ui.components.ErrorScreen
import com.example.simple.ui.components.LoadingScreen
import com.example.simple.ui.theme.SimpleColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

@Composable
fun AdminDashboardScreen(
    onNavigateToAddItem: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val requestsState by viewModel.requestsState.collectAsState()
    val membersState by viewModel.membersState.collectAsState()
    val activeOrg by viewModel.activeOrganization.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard Petugas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    activeOrg?.let {
                        val roleLabel = when(userRole) {
                            UserRole.ADMIN -> "Manager"
                            UserRole.STAFF -> "Operator"
                            else -> "Member"
                        }
                        Text(
                            text = "${it.name} ($roleLabel)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Add Item Button (Manager/Admin only)
                if (userRole == UserRole.ADMIN) {
                    IconButton(
                        onClick = onNavigateToAddItem,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Barang", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            activeOrg?.inviteCode?.let { code ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kode Undangan Anggota", style = MaterialTheme.typography.labelMedium)
                            Text(code, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(code)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin Kode")
                        }
                    }
                }
            }

            // Member Management (Manager only)
            if (userRole == UserRole.ADMIN) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Kelola Anggota",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (val state = membersState) {
                    is AdminMembersState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    is AdminMembersState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                    is AdminMembersState.Success -> {
                        state.members.forEach { member ->
                            val memberRole = member.organizations.firstOrNull()?.role
                            if (memberRole != UserRole.ADMIN) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(member.name, fontWeight = FontWeight.Bold)
                                            Text(member.email, style = MaterialTheme.typography.bodySmall)
                                            Text("Role: ${memberRole?.name}", style = MaterialTheme.typography.labelSmall)
                                        }
                                        
                                        if (memberRole == UserRole.BORROWER) {
                                            TextButton(onClick = { viewModel.changeMemberRole(member.id, UserRole.STAFF) }) {
                                                Text("Jadikan Operator")
                                            }
                                        } else {
                                            TextButton(onClick = { viewModel.changeMemberRole(member.id, UserRole.BORROWER) }) {
                                                Text("Turunkan Role")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Permintaan & Pengembalian",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = requestsState) {
                is AdminRequestsState.Loading -> LoadingScreen()
                is AdminRequestsState.Error -> ErrorScreen(message = state.message, onRetry = viewModel::observeRequests)
                is AdminRequestsState.Success -> {
                    if (state.requests.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Tidak ada antrean pekerjaan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        state.requests.forEach { request ->
                            PendingRequestCard(
                                request = request,
                                showActions = userRole == UserRole.STAFF,
                                onApprove = { viewModel.approve(request.id) },
                                onReject = { viewModel.reject(request.id) },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: BorrowRequest,
    showActions: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = request.itemName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Diajukan oleh: ${request.requesterName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Qty: ${request.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${dateFormatter.format(Date(request.startDate))} → ${dateFormatter.format(Date(request.endDate))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            request.notes?.let {
                Text(text = "Catatan: $it", style = MaterialTheme.typography.bodyMedium)
            }

            if (showActions) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                        Text("Tolak")
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SimpleColors.Success),
                    ) {
                        Text("Setujui")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Akses Operator (Staff) diperlukan untuk tindakan ini",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
