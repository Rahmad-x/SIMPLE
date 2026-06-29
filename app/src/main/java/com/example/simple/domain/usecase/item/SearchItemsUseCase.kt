package com.example.simple.domain.usecase.item

import com.example.simple.data.repository.ItemRepository
import com.example.simple.domain.model.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchItemsUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    fun observe(orgId: String, query: String): Flow<List<Item>> =
        itemRepository.observeItems(orgId, search = query)
}
