package com.example.simple.domain.usecase.item

import com.example.simple.common.Result
import com.example.simple.data.repository.ItemRepository
import com.example.simple.domain.model.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    fun observe(orgId: String, search: String = "", category: String? = null, forceRefresh: Boolean = false): Flow<List<Item>> =
        itemRepository.observeItems(orgId, search, category, forceRefresh)

    fun observeGlobal(search: String = ""): Flow<List<Item>> =
        itemRepository.searchGlobalItems(search)

    suspend fun refresh(orgId: String): Result<Unit> = itemRepository.refreshItems(orgId)
}
