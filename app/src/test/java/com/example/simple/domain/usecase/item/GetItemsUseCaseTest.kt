package com.example.simple.domain.usecase.item

import com.example.simple.data.repository.ItemRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GetItemsUseCaseTest {

    private val itemRepository = mock(ItemRepository::class.java)
    private val getItemsUseCase = GetItemsUseCase(itemRepository)

    @Test
    fun observeShouldReturnItemsFromRepository() = runTest {
        val orgId = "org1"
        val expectedItems = listOf(
            Item(
                id = "1",
                organizationId = orgId,
                name = "Test Item",
                category = "Test",
                location = "Test",
                totalStock = 10,
                availableStock = 5,
                condition = ItemCondition.GOOD,
                status = ItemStatus.AVAILABLE
            )
        )
        `when`(itemRepository.observeItems(orgId)).thenReturn(flowOf(expectedItems))

        val resultFlow = getItemsUseCase.observe(orgId)
        
        resultFlow.collect { items ->
            assertEquals(expectedItems, items)
        }
    }
}
