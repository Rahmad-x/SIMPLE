package com.example.simple.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.usecase.item.GetItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

    private var currentOrgId: String? = null

    init {
        viewModelScope.launch {
            val orgId = organizationRepository.activeOrgIdFlow.first()
            currentOrgId = orgId
            if (orgId == null) {
                _catalogState.value = CatalogState.Error("Organisasi tidak ditemukan")
                return@launch
            }
            getItemsUseCase.refresh(orgId)

            combine(_searchQuery, _selectedCategory) { query, category -> query to category }
                .flatMapLatest { (query, category) -> getItemsUseCase.observe(orgId, query, category) }
                .collect { _catalogState.value = CatalogState.Success(it) }
        }
    }

    fun updateSearch(value: String) { _searchQuery.value = value }
    fun updateCategory(category: String?) { _selectedCategory.value = category }

    fun refresh() {
        viewModelScope.launch {
            currentOrgId?.let { getItemsUseCase.refresh(it) }
        }
    }
}