package com.example.simple.ui.screens.login

import com.example.simple.domain.model.User // Pastikan import model User kamu sudah benar

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    // UBAH LoginResponse MENJADI User DI SINI:
    data class Success(val user: User) : LoginState
    data class Error(val message: String) : LoginState
}