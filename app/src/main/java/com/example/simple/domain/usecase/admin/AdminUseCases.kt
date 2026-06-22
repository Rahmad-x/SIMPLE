package com.example.simple.domain.usecase.admin

import com.example.simple.common.Result
import com.example.simple.data.remote.dto.request.CreateItemRequest
import com.example.simple.data.remote.dto.request.UpdateItemRequest
import com.example.simple.data.repository.AdminRepository
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.User
import javax.inject.Inject

class GetPendingRequestsUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String): Result<List<BorrowRequest>> =
        adminRepository.getPendingRequests(orgId)
}

class ApproveBorrowRequestUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String, requestId: String): Result<Unit> =
        adminRepository.approveRequest(orgId, requestId)
}

class RejectBorrowRequestUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String, requestId: String, reason: String): Result<Unit> =
        adminRepository.rejectRequest(orgId, requestId, reason)
}

class ManageItemUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend fun add(orgId: String, request: CreateItemRequest): Result<Item> =
        adminRepository.addItem(orgId, request)

    suspend fun update(orgId: String, itemId: String, request: UpdateItemRequest): Result<Item> =
        adminRepository.updateItem(orgId, itemId, request)

    suspend fun delete(orgId: String, itemId: String): Result<Unit> =
        adminRepository.deleteItem(orgId, itemId)
}

class GetMembersUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String): Result<List<User>> = adminRepository.getMembers(orgId)
}