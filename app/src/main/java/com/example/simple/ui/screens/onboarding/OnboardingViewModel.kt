package com.example.simple.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.domain.model.Organization
import com.example.simple.domain.usecase.organization.CreateOrganizationUseCase
import com.example.simple.domain.usecase.organization.JoinOrganizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingState {
    data object Idle : OnboardingState()
    data object Loading : OnboardingState()
    data class Success(val organization: Organization) : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}

enum class OnboardingMode { CREATE, JOIN }

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createOrganizationUseCase: CreateOrganizationUseCase,
    private val joinOrganizationUseCase: JoinOrganizationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _mode = MutableStateFlow(OnboardingMode.CREATE)
    val mode: StateFlow<OnboardingMode> = _mode.asStateFlow()

    private val _orgName = MutableStateFlow("")
    val orgName: StateFlow<String> = _orgName.asStateFlow()

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode.asStateFlow()

    fun setMode(mode: OnboardingMode) { _mode.value = mode }
    fun updateOrgName(value: String) { _orgName.value = value }
    fun updateInviteCode(value: String) { _inviteCode.value = value }

    fun submit() {
        viewModelScope.launch {
            _state.value = OnboardingState.Loading
            val result = if (_mode.value == OnboardingMode.CREATE) {
                createOrganizationUseCase(_orgName.value)
            } else {
                joinOrganizationUseCase(_inviteCode.value)
            }
            _state.value = when (result) {
                is Result.Success -> OnboardingState.Success(result.data)
                is Result.Error -> OnboardingState.Error(result.message)
                is Result.Loading -> OnboardingState.Loading
            }
        }
    }
}