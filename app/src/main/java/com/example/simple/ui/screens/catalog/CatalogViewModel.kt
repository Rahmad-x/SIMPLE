package com.example.simple.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.UserRole
import com.example.simple.domain.usecase.item.GetItemsUseCase
import com.example.simple.domain.usecase.item.DeleteItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CatalogState {
    data object Loading : CatalogState()
    data class Success(val items: List<Item>) : CatalogState()
    data class Error(val message: String) : CatalogState()
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    private var currentOrgId: String? = null
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userRole: StateFlow<UserRole?> = organizationRepository.activeOrgIdFlow
        .flatMapLatest { id ->
            if (id==null) flow {emit(null)}
            else flow {
                val result = organizationRepository.getOrganizationDetails(id)
                emit((result as? Result.Success)?.data?.role)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            val orgId = organizationRepository.activeOrgIdFlow.first()
            currentOrgId = orgId
            if (orgId == null) {
                _catalogState.value = CatalogState.Error("Organisasi tidak ditemukan")
                return@launch
            }
            
            combine(_searchQuery, _selectedCategory, _isRefreshing) { query, category, refreshing -> 
                Triple(query, category, refreshing)
            }.flatMapLatest { (query, category, refreshing) -> 
                if (query.isNotEmpty()) {
                    getItemsUseCase.observeGlobal(query)
                } else {
                    getItemsUseCase.observe(orgId, query, category, forceRefresh = refreshing)
                }
            }.collect { 
                _catalogState.value = CatalogState.Success(it)
                _isRefreshing.value = false
            }
        }
    }

    fun updateSearch(value: String) { _searchQuery.value = value }
    fun updateCategory(category: String?) { _selectedCategory.value = category }

    fun refresh() {
        _isRefreshing.value = true
    }
    fun deleteItem(itemId:String){
        viewModelScope.launch {
            val orgId = currentOrgId ?: return@launch
            deleteItemUseCase(orgId, itemId)
        }
    }
}