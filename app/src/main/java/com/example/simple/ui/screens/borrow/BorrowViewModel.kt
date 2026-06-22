package com.example.simple.ui.screens.borrow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.usecase.borrow.SubmitBorrowRequestUseCase
import com.example.simple.domain.usecase.item.GetItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BorrowSubmitState {
    data object Idle : BorrowSubmitState()
    data object Loading : BorrowSubmitState()
    data object Success : BorrowSubmitState()
    data class Error(val message: String) : BorrowSubmitState()
}

@HiltViewModel
class BorrowViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase,
    private val submitBorrowRequestUseCase: SubmitBorrowRequestUseCase,
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _startDate = MutableStateFlow(System.currentTimeMillis())
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis() + 86_400_000L)
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _submitState = MutableStateFlow<BorrowSubmitState>(BorrowSubmitState.Idle)
    val submitState: StateFlow<BorrowSubmitState> = _submitState.asStateFlow()

    private var orgId: String? = null

    init {
        viewModelScope.launch {
            orgId = organizationRepository.activeOrgIdFlow.first()
            orgId?.let { id ->
                getItemsUseCase.observe(id).collect { _items.value = it.filter { item -> item.isAvailable } }
            }
        }
    }

    fun selectItem(item: Item) { _selectedItem.value = item }
    fun updateQuantity(value: Int) { _quantity.value = value.coerceAtLeast(1) }
    fun updateStartDate(value: Long) { _startDate.value = value }
    fun updateEndDate(value: Long) { _endDate.value = value }
    fun updateNotes(value: String) { _notes.value = value }

    fun submit() {
        val item = _selectedItem.value ?: return
        val org = orgId ?: return
        viewModelScope.launch {
            _submitState.value = BorrowSubmitState.Loading
            val result = submitBorrowRequestUseCase(
                orgId = org,
                itemId = item.id,
                quantity = _quantity.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                notes = _notes.value.ifBlank { null },
            )
            _submitState.value = when (result) {
                is Result.Success -> BorrowSubmitState.Success
                is Result.Error -> BorrowSubmitState.Error(result.message)
                is Result.Loading -> BorrowSubmitState.Loading
            }
        }
    }

    fun resetSelection() {
        _selectedItem.value = null
        _quantity.value = 1
        _notes.value = ""
        _submitState.value = BorrowSubmitState.Idle
    }
}