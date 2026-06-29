package com.example.simple.domain.usecase.item

import com.example.simple.common.Result
import com.example.simple.data.repository.ItemRepository
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
) {
    suspend operator fun invoke(orgId: String, itemId: String): Result<Unit> =
        itemRepository.deleteItem(orgId, itemId)
}
