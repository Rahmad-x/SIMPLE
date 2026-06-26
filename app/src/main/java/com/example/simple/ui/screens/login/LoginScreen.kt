package com.example.simple.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.simple.ui.components.PrimaryButton
import com.example.simple.ui.components.SimpleTextField

@Composable
fun LoginScreen(
    onLoginSuccess: (com.example.simple.domain.model.User) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val loginState by viewModel.loginState.collectAsState()
    val isSignUpMode by viewModel.isSignUpMode.collectAsState()
    val isResetMode by viewModel.isResetMode.collectAsState()
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    LaunchedEffect(loginState) {
        val state = loginState
        if (state is LoginState.Success) onLoginSuccess(state.user)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp),
                        )
                        .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(32.dp),
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Column {
                    Text(
                        text = "SIMPLE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Inventory Management",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (isResetMode) "Reset Password" else if (isSignUpMode) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (isResetMode) {
                            "Enter your email to receive a reset link"
                        } else if (isSignUpMode) {
                            "Join SIMPLE and start managing inventory"
                        } else {
                            "Sign in to manage your inventory"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isSignUpMode) {
                        SimpleTextField(
                            value = name,
                            onValueChange = viewModel::updateName,
                            label = "Full Name",
                            placeholder = "Rahmad",
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    SimpleTextField(
                        value = email,
                        onValueChange = viewModel::updateEmail,
                        label = "Email Address",
                        placeholder = "you@gmail.com",
                        keyboardType = KeyboardType.Email,
                    )

                    if (!isResetMode) {
                        Spacer(modifier = Modifier.height(12.dp))

                        SimpleTextField(
                            value = password,
                            onValueChange = viewModel::updatePassword,
                            label = "Password",
                            placeholder = "••••••••",
                            isPassword = true,
                            isError = loginState is LoginState.Error,
                            errorMessage = (loginState as? LoginState.Error)?.message,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = if (isResetMode) "Send Reset Link" else if (isSignUpMode) "Sign Up" else "Sign In",
                        onClick = viewModel::submit,
                        loading = loginState is LoginState.Loading,
                    )

                    if (loginState is LoginState.Error && isResetMode) {
                        Text(
                            text = (loginState as LoginState.Error).message,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isResetMode) {
                        TextButton(onClick = viewModel::toggleMode, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isSignUpMode) {
                                    "Sudah punya akun? Sign In"
                                } else {
                                    "Belum punya akun? Sign Up"
                                },
                            )
                        }
                    }

                    TextButton(onClick = viewModel::toggleResetMode, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isResetMode) "Back to Login" else "Forgot Password?",
                        )
                    }
                }
            }
        }
    }
}