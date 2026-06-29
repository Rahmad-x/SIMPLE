package com.example.simple.domain.usecase.item

import com.example.simple.common.Result
import com.example.simple.data.repository.ItemRepository
import com.example.simple.domain.model.Item
import javax.inject.Inject

class GetItemDetailUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(orgId: String, itemId: String): Result<Item> =
        itemRepository.getItemDetail(orgId, itemId)
}
