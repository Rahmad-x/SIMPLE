package com.example.simple.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.usecase.admin.ApproveBorrowRequestUseCase
import com.example.simple.domain.usecase.admin.GetPendingRequestsUseCase
import com.example.simple.domain.usecase.admin.RejectBorrowRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminRequestsState {
    data object Loading : AdminRequestsState()
    data class Success(val requests: List<BorrowRequest>) : AdminRequestsState()
    data class Error(val message: String) : AdminRequestsState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val approveBorrowRequestUseCase: ApproveBorrowRequestUseCase,
    private val rejectBorrowRequestUseCase: RejectBorrowRequestUseCase,
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {

    private val _requestsState = MutableStateFlow<AdminRequestsState>(AdminRequestsState.Loading)
    val requestsState: StateFlow<AdminRequestsState> = _requestsState.asStateFlow()

    private var orgId: String? = null

    init { loadRequests() }

    fun loadRequests() {
        viewModelScope.launch {
            _requestsState.value = AdminRequestsState.Loading
            val id = organizationRepository.activeOrgIdFlow.first()
            orgId = id
            if (id == null) {
                _requestsState.value = AdminRequestsState.Error("Organisasi tidak ditemukan")
                return@launch
            }
            when (val result = getPendingRequestsUseCase(id)) {
                is Result.Success -> _requestsState.value = AdminRequestsState.Success(result.data)
                is Result.Error -> _requestsState.value = AdminRequestsState.Error(result.message)
                is Result.Loading -> Unit
            }
        }
    }

    fun approve(requestId: String) {
        val id = orgId ?: return
        viewModelScope.launch {
            approveBorrowRequestUseCase(id, requestId)
            loadRequests()
        }
    }

    fun reject(requestId: String, reason: String = "Ditolak oleh admin") {
        val id = orgId ?: return
        viewModelScope.launch {
            rejectBorrowRequestUseCase(id, requestId, reason)
            loadRequests()
        }
    }
}