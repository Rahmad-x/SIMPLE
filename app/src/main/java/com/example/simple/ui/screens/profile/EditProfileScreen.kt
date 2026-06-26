package com.example.simple.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.SimpleTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()

    LaunchedEffect(state) {
        if (state is EditProfileState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Avatar Placeholder (Simulasi upload foto)
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (name.isNotEmpty()) name[0].uppercase() else "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { /* TODO: Image Picker */ },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Ganti Foto",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SimpleTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = "Nama Lengkap",
                placeholder = "Contoh: Rahmad Maulana"
            )

            SimpleTextField(
                value = phone,
                onValueChange = viewModel::updatePhone,
                label = "Nomor Telepon",
                placeholder = "Contoh: 08123456789",
                keyboardType = KeyboardType.Phone
            )

            SimpleTextField(
                value = avatarUrl,
                onValueChange = viewModel::updateAvatarUrl,
                label = "URL Foto Profil (Optional)",
                placeholder = "https://link-foto.com/foto.jpg"
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state is EditProfileState.Error) {
                Text(
                    text = (state as EditProfileState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryButton(
                text = "Simpan Perubahan",
                onClick = viewModel::submit,
                loading = state is EditProfileState.Loading
            )
        }
    }
}
