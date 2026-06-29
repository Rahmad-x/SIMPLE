package com.example.simple.domain.usecase.admin

import com.example.simple.common.Result
import com.example.simple.data.repository.AdminRepository
import com.example.simple.domain.model.Item
import javax.inject.Inject

class GetExternalProductsUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(orgId: String): Result<List<Item>> =
        adminRepository.getExternalProducts(orgId)
}
