package com.example.simple.domain.usecase.auth

import com.example.simple.common.Result
import com.example.simple.data.repository.AuthRepository
import com.example.simple.domain.model.User
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error("Email dan password tidak boleh kosong")
        }
        if (password.length < 8) {
            return Result.Error("Password minimal 8 karakter")
        }
        return authRepository.login(email.trim(), password)
    }
}

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        if (name.isBlank()) return Result.Error("Nama tidak boleh kosong")
        if (email.isBlank()) return Result.Error("Email tidak boleh kosong")
        if (password.length < 8) return Result.Error("Password minimal 8 karakter")
        return authRepository.signup(name.trim(), email.trim(), password)
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Result<Unit> = authRepository.resetPassword(email)
}

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(name: String, phone: String? = null, avatar: String? = null): Result<Unit> =
        authRepository.updateProfile(name, phone, avatar)
}