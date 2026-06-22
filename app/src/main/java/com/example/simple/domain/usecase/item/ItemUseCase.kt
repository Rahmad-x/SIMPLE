package com.example.simple.domain.usecase.item

import com.example.simple.common.Result
import com.example.simple.data.repository.ItemRepository
import com.example.simple.domain.model.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    fun observe(orgId: String, search: String = "", category: String? = null): Flow<List<Item>> =
        itemRepository.observeItems(orgId, search, category)

    suspend fun refresh(orgId: String): Result<Unit> = itemRepository.refreshItems(orgId)
}

class SearchItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    fun observe(orgId: String, query: String): Flow<List<Item>> =
        itemRepository.observeItems(orgId, search = query)
}

class GetItemDetailUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(orgId: String, itemId: String): Result<Item> =
        itemRepository.getItemDetail(orgId, itemId)
}