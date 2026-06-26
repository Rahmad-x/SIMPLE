package com.example.simple.domain.usecase.admin

import com.example.simple.data.repository.ItemRepository
import com.example.simple.data.repository.TransactionRepository
import com.example.simple.domain.model.ItemStatus
import com.example.simple.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AdminDashboardSummary(
    val totalItems: Int,
    val availableItems: Int,
    val borrowedItems: Int,
    val pendingRequests: Int
)

class GetAdminDashboardSummaryUseCase @Inject constructor(
    private val itemRepository: ItemRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(orgId: String): Flow<AdminDashboardSummary> {
        return combine(
            itemRepository.observeItems(orgId),
            transactionRepository.observeTransactions(orgId)
        ) { items, transactions ->
            AdminDashboardSummary(
                totalItems = items.sumOf { it.totalStock },
                availableItems = items.sumOf { it.availableStock },
                borrowedItems = transactions.count { it.status == TransactionStatus.BORROWED },
                pendingRequests = transactions.count { it.status == TransactionStatus.PENDING }
            )
        }
    }
}