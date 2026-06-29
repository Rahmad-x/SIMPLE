package com.example.simple.domain.usecase.admin

import com.example.simple.common.Result
import com.example.simple.data.repository.AdminRepository
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.User
import com.example.simple.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePendingRequestsUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    operator fun invoke(orgId: String): Flow<List<BorrowRequest>> =
        adminRepository.observePendingRequests(orgId)
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
    suspend fun add(orgId: String, item: Item): Result<Unit> =
        adminRepository.addItem(orgId, item)

    suspend fun update(orgId: String, item: Item): Result<Unit> =
        adminRepository.updateItem(orgId, item)

    suspend fun delete(orgId: String, itemId: String): Result<Unit> =
        adminRepository.deleteItem(orgId, itemId)

    suspend fun uploadImage(orgId: String, itemId: String, uri: android.net.Uri): Result<String> =
        adminRepository.uploadItemImage(orgId, itemId, uri)
}

class GetMembersUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String): Result<List<User>> = adminRepository.getMembers(orgId)
}

class UpdateMemberRoleUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String, userId: String, newRole: UserRole): Result<Unit> =
        adminRepository.updateMemberRole(orgId, userId, newRole)
}
