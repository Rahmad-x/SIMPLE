package com.example.simple.domain.usecase.borrow

import com.example.simple.common.Result
import com.example.simple.data.repository.BorrowRepository
import com.example.simple.data.repository.TransactionRepository
import com.example.simple.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmitBorrowRequestUseCase @Inject constructor(
    private val borrowRepository: BorrowRepository,
) {
    suspend operator fun invoke(
        orgId: String,
        itemId: String,
        quantity: Int,
        startDate: Long,
        endDate: Long,
        notes: String?,
    ): Result<Unit> {
        if (quantity <= 0) return Result.Error("Jumlah barang harus lebih dari 0")
        if (endDate < startDate) return Result.Error("Tanggal kembali tidak boleh sebelum tanggal pinjam")
        return borrowRepository.submitBorrowRequest(orgId, itemId, quantity, startDate, endDate, notes)
    }
}

class GetBorrowRequestsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    fun observe(orgId: String, status: String? = null): Flow<List<Transaction>> =
        transactionRepository.observeTransactions(orgId, status)

    suspend fun refresh(orgId: String, status: String? = null): Result<Unit> =
        transactionRepository.refreshTransactions(orgId, status)
}

class ReturnItemUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(orgId: String, transactionId: String): Result<Unit> =
        transactionRepository.returnItem(orgId, transactionId)
}