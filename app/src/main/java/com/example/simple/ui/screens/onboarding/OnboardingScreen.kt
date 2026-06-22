package com.example.simple.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.SimpleTextField

@Composable
fun OnboardingScreen(
    onSetupComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val orgName by viewModel.orgName.collectAsState()
    val inviteCode by viewModel.inviteCode.collectAsState()

    LaunchedEffect(state) {
        if (state is OnboardingState.Success) onSetupComplete()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Setup Organisasi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Buat organisasi baru atau join dengan kode undangan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode tabs
            Row(modifier = Modifier.fillMaxWidth()) {
                ModeTab(
                    label = "Buat Baru",
                    selected = mode == OnboardingMode.CREATE,
                    onClick = { viewModel.setMode(OnboardingMode.CREATE) },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.height(0.dp))
                ModeTab(
                    label = "Join Existing",
                    selected = mode == OnboardingMode.JOIN,
                    onClick = { viewModel.setMode(OnboardingMode.JOIN) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (mode == OnboardingMode.CREATE) {
                        Text(
                            text = "Anda akan menjadi Admin untuk organisasi ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SimpleTextField(
                            value = orgName,
                            onValueChange = viewModel::updateOrgName,
                            label = "Nama Organisasi",
                            placeholder = "Contoh: Lab Komputer Kampus",
                        )
                    } else {
                        Text(
                            text = "Masukkan kode undangan dari Admin organisasi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SimpleTextField(
                            value = inviteCode,
                            onValueChange = viewModel::updateInviteCode,
                            label = "Kode Undangan",
                            placeholder = "Contoh: ABCD1234",
                        )
                    }

                    if (state is OnboardingState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (state as OnboardingState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Lanjutkan",
                        onClick = viewModel::submit,
                        loading = state is OnboardingState.Loading,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}