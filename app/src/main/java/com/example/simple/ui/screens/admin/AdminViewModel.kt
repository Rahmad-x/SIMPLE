package com.example.simple.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.model.Organization
import com.example.simple.domain.model.UserRole
import com.example.simple.domain.usecase.admin.ApproveBorrowRequestUseCase
import com.example.simple.domain.usecase.admin.GetExternalProductsUseCase
import com.example.simple.domain.usecase.admin.GetMembersUseCase
import com.example.simple.domain.usecase.admin.ManageItemUseCase
import com.example.simple.domain.usecase.admin.ObservePendingRequestsUseCase
import com.example.simple.domain.usecase.admin.RejectBorrowRequestUseCase
import com.example.simple.domain.usecase.admin.UpdateMemberRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminRequestsState {
    data object Loading : AdminRequestsState()
    data class Success(val requests: List<BorrowRequest>) : AdminRequestsState()
    data class Error(val message: String) : AdminRequestsState()
}

sealed class AdminMembersState {
    data object Loading : AdminMembersState()
    data class Success(val members: List<com.example.simple.domain.model.User>) : AdminMembersState()
    data class Error(val message: String) : AdminMembersState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val observePendingRequestsUseCase: ObservePendingRequestsUseCase,
    private val approveBorrowRequestUseCase: ApproveBorrowRequestUseCase,
    private val rejectBorrowRequestUseCase: RejectBorrowRequestUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val updateMemberRoleUseCase: UpdateMemberRoleUseCase,
    private val getExternalProductsUseCase: GetExternalProductsUseCase,
    private val manageItemUseCase: ManageItemUseCase,
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {

    private val _requestsState = MutableStateFlow<AdminRequestsState>(AdminRequestsState.Loading)
    val requestsState: StateFlow<AdminRequestsState> = _requestsState.asStateFlow()

    private val _membersState = MutableStateFlow<AdminMembersState>(AdminMembersState.Loading)
    val membersState: StateFlow<AdminMembersState> = _membersState.asStateFlow()

    private val _importState = MutableStateFlow<Result<String>?>(null)
    val importState = _importState.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeOrganization: StateFlow<Organization?> = organizationRepository.activeOrgIdFlow
        .flatMapLatest { id ->
            if (id == null) flow { emit(null) }
            else flow {
                val result = organizationRepository.getOrganizationDetails(id)
                emit((result as? Result.Success)?.data)
            }
        }
        .onEach { if (it != null) loadMembers(it.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userRole: StateFlow<UserRole?> = activeOrganization
        .map { it?.role }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        observeRequests()
    }

    private fun loadMembers(orgId: String) {
        viewModelScope.launch {
            _membersState.value = AdminMembersState.Loading
            when (val result = getMembersUseCase(orgId)) {
                is Result.Success -> _membersState.value = AdminMembersState.Success(result.data)
                is Result.Error -> _membersState.value = AdminMembersState.Error(result.message)
                is Result.Loading -> Unit
            }
        }
    }

    fun changeMemberRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            val orgId = organizationRepository.activeOrgIdFlow.first() ?: return@launch
            updateMemberRoleUseCase(orgId, userId, newRole)
            loadMembers(orgId)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeRequests() {
        viewModelScope.launch {
            _requestsState.value = AdminRequestsState.Loading
            organizationRepository.activeOrgIdFlow.flatMapLatest { id ->
                if (id == null) flow { emit(emptyList<BorrowRequest>()) }
                else observePendingRequestsUseCase(id)
            }.catch { e ->
                _requestsState.value = AdminRequestsState.Error(e.message ?: "Terjadi kesalahan")
            }.collect { requests ->
                _requestsState.value = AdminRequestsState.Success(requests)
            }
        }
    }

    fun approve(requestId: String) {
        viewModelScope.launch {
            val id = organizationRepository.activeOrgIdFlow.first() ?: return@launch
            approveBorrowRequestUseCase(id, requestId)
        }
    }

    fun reject(requestId: String, reason: String = "Ditolak oleh admin") {
        viewModelScope.launch {
            val id = organizationRepository.activeOrgIdFlow.first() ?: return@launch
            rejectBorrowRequestUseCase(id, requestId, reason)
        }
    }

    fun importExternalProducts() {
        viewModelScope.launch {
            val orgId = organizationRepository.activeOrgIdFlow.first() ?: return@launch
            _importState.value = Result.Loading
            when (val result = getExternalProductsUseCase(orgId)) {
                is Result.Success -> {
                    // Just take top 3 for demo
                    result.data.take(3).forEach { 
                        manageItemUseCase.add(orgId, it)
                    }
                    _importState.value = Result.Success("Berhasil mengimpor 3 produk dari FakeStore API")
                }
                is Result.Error -> {
                    _importState.value = Result.Error(result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }
    
    fun clearImportState() { _importState.value = null }
}
