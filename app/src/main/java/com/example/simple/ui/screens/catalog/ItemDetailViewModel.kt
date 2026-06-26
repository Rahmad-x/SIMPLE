package com.example.simple.ui.screens.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.domain.model.Item
import com.example.simple.domain.usecase.borrow.SubmitBorrowRequestUseCase
import com.example.simple.domain.usecase.item.GetItemDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ItemDetailState {
    data object Loading : ItemDetailState()
    data class Success(val item: Item) : ItemDetailState()
    data class Error(val message: String) : ItemDetailState()
}

sealed class BorrowState {
    data object Idle : BorrowState()
    data object Loading : BorrowState()
    data object Success : BorrowState()
    data class Error(val message: String) : BorrowState()
}

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val getItemDetailUseCase: GetItemDetailUseCase,
    private val submitBorrowRequestUseCase: SubmitBorrowRequestUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orgId: String = checkNotNull(savedStateHandle["orgId"])
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _state = MutableStateFlow<ItemDetailState>(ItemDetailState.Loading)
    val state: StateFlow<ItemDetailState> = _state.asStateFlow()

    private val _borrowState = MutableStateFlow<BorrowState>(BorrowState.Idle)
    val borrowState: StateFlow<BorrowState> = _borrowState.asStateFlow()

    init {
        loadItem()
    }

    fun loadItem() {
        viewModelScope.launch {
            _state.value = ItemDetailState.Loading
            when (val result = getItemDetailUseCase(orgId, itemId)) {
                is Result.Success -> _state.value = ItemDetailState.Success(result.data)
                is Result.Error -> _state.value = ItemDetailState.Error(result.message)
                is Result.Loading -> Unit
            }
        }
    }

    fun borrow(quantity: Int, startDate: Long, endDate: Long, notes: String?) {
        viewModelScope.launch {
            _borrowState.value = BorrowState.Loading
            val result = submitBorrowRequestUseCase(orgId, itemId, quantity, startDate, endDate, notes)
            _borrowState.value = when (result) {
                is Result.Success -> BorrowState.Success
                is Result.Error -> BorrowState.Error(result.message)
                is Result.Loading -> BorrowState.Loading
            }
        }
    }
}
