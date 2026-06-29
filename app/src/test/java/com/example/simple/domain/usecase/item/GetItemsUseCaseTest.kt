package com.example.simple.domain.usecase.item

import com.example.simple.data.repository.ItemRepository
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class GetItemsUseCaseTest {

    @Mock
    private lateinit var itemRepository: ItemRepository
    private lateinit var getItemsUseCase: GetItemsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getItemsUseCase = GetItemsUseCase(itemRepository)
    }

    @Test
    fun `observe should return items from repository`() = runTest {
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
