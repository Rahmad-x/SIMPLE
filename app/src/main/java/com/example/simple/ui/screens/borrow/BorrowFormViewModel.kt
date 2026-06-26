package com.example.simple.ui.screens.borrow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.usecase.borrow.SubmitBorrowRequestUseCase
import com.example.simple.domain.usecase.item.GetItemDetailUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class BorrowFormState {
    data object Idle : BorrowFormState()
    data object Loading : BorrowFormState()
    data object Success : BorrowFormState()
    data class Error(val message: String) : BorrowFormState()
}

@HiltViewModel
class BorrowFormViewModel @Inject constructor(
    private val getItemDetailUseCase: GetItemDetailUseCase,
    private val submitBorrowRequestUseCase: SubmitBorrowRequestUseCase,
    private val organizationRepository: OrganizationRepository,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemOrgId: String = checkNotNull(savedStateHandle["orgId"])
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _item = MutableStateFlow<Item?>(null)
    val item: StateFlow<Item?> = _item.asStateFlow()

    private val _requesterName = MutableStateFlow("")
    val requesterName: StateFlow<String> = _requesterName.asStateFlow()

    private val _requesterOrgName = MutableStateFlow("")
    val requesterOrgName: StateFlow<String> = _requesterOrgName.asStateFlow()

    private val _quantity = MutableStateFlow("1")
    val quantity: StateFlow<String> = _quantity.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _startDate = MutableStateFlow(System.currentTimeMillis())
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis() + 86400000) // Default 1 day
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    private val _state = MutableStateFlow<BorrowFormState>(BorrowFormState.Idle)
    val state: StateFlow<BorrowFormState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load item details
            val itemResult = getItemDetailUseCase(itemOrgId, itemId)
            if (itemResult is Result.Success) {
                _item.value = itemResult.data
            }

            // Load user info
            val userId = sessionManager.userIdFlow.first()
            if (userId != null) {
                val userDoc = firestore.collection("users").document(userId).get().await()
                _requesterName.value = userDoc.getString("name") ?: ""
            }

            // Load active organization info
            val myOrgId = sessionManager.activeOrgIdFlow.first()
            if (myOrgId != null) {
                val orgDoc = firestore.collection("organizations").document(myOrgId).get().await()
                _requesterOrgName.value = orgDoc.getString("name") ?: ""
            }
        }
    }

    fun updateQuantity(value: String) { _quantity.value = value }
    fun updateNotes(value: String) { _notes.value = value }
    fun updateStartDate(value: Long) { _startDate.value = value }
    fun updateEndDate(value: Long) { _endDate.value = value }

    fun submit() {
        val qty = _quantity.value.toIntOrNull() ?: 1
        viewModelScope.launch {
            _state.value = BorrowFormState.Loading
            val result = submitBorrowRequestUseCase(
                orgId = itemOrgId,
                itemId = itemId,
                quantity = qty,
                startDate = _startDate.value,
                endDate = _endDate.value,
                notes = _notes.value.ifBlank { null }
            )

            _state.value = when (result) {
                is Result.Success -> BorrowFormState.Success
                is Result.Error -> BorrowFormState.Error(result.message)
                is Result.Loading -> BorrowFormState.Loading
            }
        }
    }
}
