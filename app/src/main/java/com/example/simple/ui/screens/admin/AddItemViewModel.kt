package com.example.simple.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus
import com.example.simple.domain.usecase.admin.ManageItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class AddItemState {
    data object Idle : AddItemState()
    data object Loading : AddItemState()
    data object Success : AddItemState()
    data class Error(val message: String) : AddItemState()
}

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val manageItemUseCase: ManageItemUseCase,
    private val organizationRepository: OrganizationRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AddItemState>(AddItemState.Idle)
    val state: StateFlow<AddItemState> = _state.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _totalStock = MutableStateFlow("1")
    val totalStock: StateFlow<String> = _totalStock.asStateFlow()

    private val _emoji = MutableStateFlow("📦")
    val emoji: StateFlow<String> = _emoji.asStateFlow()

    fun updateName(value: String) { _name.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updateCategory(value: String) { _category.value = value }
    fun updateLocation(value: String) { _location.value = value }
    fun updateTotalStock(value: String) { _totalStock.value = value }
    fun updateEmoji(value: String) { _emoji.value = value }

    fun submit() {
        viewModelScope.launch {
            _state.value = AddItemState.Loading
            val orgId = organizationRepository.activeOrgIdFlow.first()
            if (orgId == null) {
                _state.value = AddItemState.Error("Organisasi tidak ditemukan")
                return@launch
            }

            val stock = _totalStock.value.toIntOrNull() ?: 1
            val item = Item(
                id = UUID.randomUUID().toString(),
                organizationId = orgId,
                name = _name.value,
                description = _description.value.ifBlank { null },
                category = _category.value.ifBlank { "Lainnya" },
                location = _location.value.ifBlank { "Gudang" },
                totalStock = stock,
                availableStock = stock,
                condition = ItemCondition.GOOD,
                emoji = _emoji.value,
                status = ItemStatus.AVAILABLE
            )

            val result = manageItemUseCase.add(orgId, item)
            _state.value = when (result) {
                is Result.Success -> AddItemState.Success
                is Result.Error -> AddItemState.Error(result.message)
                is Result.Loading -> AddItemState.Loading
            }
        }
    }
}
