package com.example.simple.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.domain.usecase.auth.LoginUseCase
import com.example.simple.domain.usecase.auth.ResetPasswordUseCase
import com.example.simple.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _isResetMode = MutableStateFlow(false)
    val isResetMode: StateFlow<Boolean> = _isResetMode.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun toggleMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _isResetMode.value = false
        _loginState.value = LoginState.Idle
    }

    fun toggleResetMode() {
        _isResetMode.value = !_isResetMode.value
        _isSignUpMode.value = false
        _loginState.value = LoginState.Idle
    }

    fun updateName(value: String) { _name.value = value }
    fun updateEmail(value: String) { _email.value = value }
    fun updatePassword(value: String) { _password.value = value }

    fun submit() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            if (_isResetMode.value) {
                val result = resetPasswordUseCase(_email.value)
                _loginState.value = when (result) {
                    is Result.Success -> LoginState.Error("Email reset password telah dikirim.")
                    is Result.Error -> LoginState.Error(result.message)
                    is Result.Loading -> LoginState.Loading
                }
                return@launch
            }

            val result = if (_isSignUpMode.value) {
                signUpUseCase(_name.value, _email.value, _password.value)
            } else {
                loginUseCase(_email.value, _password.value)
            }
            _loginState.value = when (result) {
                is Result.Success -> LoginState.Success(result.data)
                is Result.Error -> LoginState.Error(result.message)
                is Result.Loading -> LoginState.Loading
            }
        }
    }
}
