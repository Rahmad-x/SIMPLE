package com.example.simple.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.domain.model.Organization
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.domain.usecase.borrow.GetBorrowRequestsUseCase
import com.example.simple.domain.usecase.item.GetItemsUseCase
import com.example.simple.domain.usecase.organization.GetOrganizationsUseCase // <--- CUKUP SATU INI SAJA
import com.example.simple.data.repository.OrganizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase,
    private val getBorrowRequestsUseCase: GetBorrowRequestsUseCase,
    private val getOrganizationsUseCase: GetOrganizationsUseCase,
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    val activeOrganization: StateFlow<Organization?> = combine(
        organizationRepository.activeOrgIdFlow,
        getOrganizationsUseCase.observe(),
    ) { activeId, orgs -> orgs.find { it.id == activeId } ?: orgs.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            val orgId = organizationRepository.activeOrgIdFlow.first()
            if (orgId == null) {
                _homeState.value = HomeState.Error("Belum ada organisasi aktif")
                return@launch
            }

            getItemsUseCase.refresh(orgId)
            getBorrowRequestsUseCase.refresh(orgId)

            combine(
                getItemsUseCase.observe(orgId),
                getBorrowRequestsUseCase.observe(orgId),
            ) { items, transactions ->
                val stats = HomeStats(
                    totalItems = items.sumOf { it.totalStock },
                    availableItems = items.sumOf { it.availableStock },
                    onLoan = transactions.count { it.status == TransactionStatus.BORROWED },
                    overdue = transactions.count { it.status == TransactionStatus.OVERDUE },
                )
                HomeState.Success(stats, transactions.take(5))
            }.collect { _homeState.value = it }
        }
    }

    fun switchOrganization(orgId: String) {
        viewModelScope.launch {
            organizationRepository.switchOrganization(orgId)
            loadHome()
        }
    }

    fun retry() = loadHome()
}