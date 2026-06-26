package com.example.simple.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.domain.usecase.auth.UpdateProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class EditProfileState {
    data object Idle : EditProfileState()
    data object Loading : EditProfileState()
    data object Success : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _state = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                _name.value = doc.getString("name") ?: ""
                _phone.value = doc.getString("phone") ?: ""
                _avatarUrl.value = doc.getString("avatar") ?: ""
            } catch (e: Exception) {
                // Ignore load errors
            }
        }
    }

    fun updateName(value: String) { _name.value = value }
    fun updatePhone(value: String) { _phone.value = value }
    fun updateAvatarUrl(value: String) { _avatarUrl.value = value }

    fun submit() {
        viewModelScope.launch {
            _state.value = EditProfileState.Loading
            val result = updateProfileUseCase(
                name = _name.value,
                phone = _phone.value.ifBlank { null },
                avatar = _avatarUrl.value.ifBlank { null }
            )
            _state.value = when (result) {
                is Result.Success -> EditProfileState.Success
                is Result.Error -> EditProfileState.Error(result.message)
                is Result.Loading -> EditProfileState.Loading
            }
        }
    }
}
