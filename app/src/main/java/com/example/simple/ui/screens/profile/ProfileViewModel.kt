package com.example.simple.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.domain.model.Organization
import com.example.simple.domain.usecase.auth.LogoutUseCase
import com.example.simple.domain.usecase.organization.GetOrganizationsUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getOrganizationsUseCase: GetOrganizationsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModel() {

    val organizations: StateFlow<List<Organization>> = getOrganizationsUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            _userEmail.value = auth.currentUser?.email ?: ""
            
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                _userName.value = doc.getString("name") ?: "User"
                _userPhone.value = doc.getString("phone") ?: ""
            } catch (e: Exception) {
                _userName.value = "User"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.value = true
        }
    }
}
