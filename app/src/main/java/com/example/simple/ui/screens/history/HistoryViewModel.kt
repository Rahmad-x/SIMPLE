package com.example.simple.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Transaction
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.domain.usecase.borrow.GetBorrowRequestsUseCase
import com.example.simple.domain.usecase.borrow.ReturnItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<TransactionStatus?>(null)
    val selectedFilter: StateFlow<TransactionStatus?> = _selectedFilter.asStateFlow()

    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    private var orgId: String? = null

    init {
        viewModelScope.launch {
            orgId = organizationRepository.activeOrgIdFlow.first()
            val id = orgId
            if (id == null) {
                _historyState.value = HistoryState.Error("Organisasi tidak ditemukan")
                return@launch
            }
            getBorrowRequestsUseCase.refresh(id)

            _selectedFilter.flatMapLatest { filter ->
                getBorrowRequestsUseCase.observe(id, filter?.name)
            }.collect { _historyState.value = HistoryState.Success(it) }
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