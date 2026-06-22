package com.example.simple.domain.usecase.organization

import com.example.simple.common.Result
import com.example.simple.data.repository.OrganizationRepository
import com.example.simple.domain.model.Organization
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrganizationsUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    fun observe(): Flow<List<Organization>> = organizationRepository.observeOrganizations()

    suspend fun refresh(): Result<List<Organization>> = organizationRepository.refreshOrganizations()
}

class CreateOrganizationUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(name: String, description: String? = null): Result<Organization> {
        if (name.isBlank()) return Result.Error("Nama organisasi tidak boleh kosong")
        return organizationRepository.createOrganization(name.trim(), description)
    }
}

class JoinOrganizationUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(inviteCode: String): Result<Organization> {
        if (inviteCode.isBlank()) return Result.Error("Kode undangan tidak boleh kosong")
        return organizationRepository.joinOrganization(inviteCode.trim())
    }
}

class SwitchOrganizationUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) {
    suspend operator fun invoke(orgId: String): Result<Unit> = organizationRepository.switchOrganization(orgId)
}