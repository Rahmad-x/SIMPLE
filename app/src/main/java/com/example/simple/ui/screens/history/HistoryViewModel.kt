package com.example.simple.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Transaction
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.domain.model.UserRole
import com.example.simple.domain.usecase.borrow.GetBorrowRequestsUseCase
import com.example.simple.domain.usecase.borrow.ReturnItemUseCase
import com.example.simple.domain.usecase.organization.GetOrganizationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryState {
    data object Loading : HistoryState()
    data class Success(val transactions: List<Transaction>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getBorrowRequestsUseCase: GetBorrowRequestsUseCase,
    private val returnItemUseCase: ReturnItemUseCase,
    private val organizationRepository: OrganizationRepository,
    private val getOrganizationsUseCase: GetOrganizationsUseCase,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<TransactionStatus?>(null)
    val selectedFilter: StateFlow<TransactionStatus?> = _selectedFilter.asStateFlow()

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userRole: StateFlow<UserRole?> = organizationRepository.activeOrgIdFlow
        .flatMapLatest { id ->
            if (id == null) flow { emit(null) }
            else getOrganizationsUseCase.observe().map { orgs ->
                orgs.find { it.id == id }?.role
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var orgId: String? = null

    init {
        observeHistory()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeHistory() {
        viewModelScope.launch {
            organizationRepository.activeOrgIdFlow.flatMapLatest { id ->
                orgId = id
                if (id == null) {
                    flow { emit(emptyList<Transaction>()) }
                } else {
                    getBorrowRequestsUseCase.refresh(id)
                    _selectedFilter.flatMapLatest { filter ->
                        getBorrowRequestsUseCase.observe(id, filter?.name)
                    }
                }
            }.catch { e ->
                _historyState.value = HistoryState.Error(e.message ?: "Terjadi kesalahan")
            }.collect { 
                _historyState.value = HistoryState.Success(it) 
            }
        }
    }

    fun setFilter(status: TransactionStatus?) { _selectedFilter.value = status }

    fun returnItem(transactionId: String) {
        val id = orgId ?: return
        viewModelScope.launch {
            returnItemUseCase(id, transactionId)
        }
    }
}
