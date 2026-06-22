package com.example.simple.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.domain.model.Organization
import com.example.simple.domain.usecase.auth.LogoutUseCase
import com.example.simple.domain.usecase.organization.GetOrganizationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getOrganizationsUseCase: GetOrganizationsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val organizations: StateFlow<List<Organization>> = getOrganizationsUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.value = true
        }
    }
}