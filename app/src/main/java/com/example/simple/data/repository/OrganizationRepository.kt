package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.domain.model.Organization
import com.example.simple.domain.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganizationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager,
) {
    val activeOrgIdFlow: Flow<String?> = sessionManager.activeOrgIdFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeOrganizations(): Flow<List<Organization>> = sessionManager.userIdFlow.flatMapLatest { userId ->
        if (userId == null) return@flatMapLatest flow { emit(emptyList()) }
        
        callbackFlow {
            val listener = firestore.collection("users")
                .document(userId)
                .collection("memberships")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val orgs = snapshot?.documents?.mapNotNull { doc ->
                        Organization(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unknown Org",
                            description = doc.getString("description"),
                            role = UserRole.fromString(doc.getString("role") ?: "BORROWER"),
                            inviteCode = doc.getString("inviteCode"),
                            lateFeePerDay = doc.getDouble("lateFeePerDay") ?: 0.0,
                            allowPaidRental = doc.getBoolean("allowPaidRental") ?: false
                        )
                    } ?: emptyList()
                    
                    trySend(orgs)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun createOrganization(name: String, description: String?): Result<Organization> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.userIdFlow.first() ?: return@withContext Result.Error("User not logged in")
            
            // Check if user already has an organization where they are ADMIN
            val existingAdminSnapshot = firestore.collection("users").document(userId)
                .collection("memberships")
                .whereEqualTo("role", UserRole.ADMIN.name)
                .get()
                .await()
            
            val role = if (existingAdminSnapshot.isEmpty) UserRole.ADMIN else UserRole.STAFF
            
            val orgData = hashMapOf(
                "name" to name,
                "description" to description,
                "createdAt" to System.currentTimeMillis(),
                "createdBy" to userId,
                "inviteCode" to (100000..999999).random().toString() // Simple invite code generation
            )
            
            val orgRef = firestore.collection("organizations").add(orgData).await()
            
            val membership = hashMapOf(
                "name" to name,
                "role" to role.name,
                "joinedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users").document(userId)
                .collection("memberships").document(orgRef.id).set(membership).await()
                
            // Update activeOrgId in user document
            firestore.collection("users").document(userId).update("activeOrgId", orgRef.id).await()

            // Also record the member in the organization document
            val orgMember = hashMapOf(
                "name" to name,
                "role" to role.name,
                "email" to (firestore.collection("users").document(userId).get().await().getString("email") ?: ""),
                "joinedAt" to System.currentTimeMillis()
            )
            firestore.collection("organizations").document(orgRef.id)
                .collection("members").document(userId).set(orgMember).await()

            val newOrg = Organization(
                id = orgRef.id,
                name = name,
                description = description,
                role = role,
                inviteCode = orgData["inviteCode"] as? String
            )
            
            sessionManager.setActiveOrganization(orgRef.id)
            Result.Success(newOrg)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal membuat organisasi")
        }
    }

    suspend fun joinOrganization(inviteCode: String): Result<Organization> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.userIdFlow.first() ?: return@withContext Result.Error("User not logged in")
            
            val orgSnapshot = firestore.collection("organizations")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()
                
            val orgDoc = orgSnapshot.documents.firstOrNull() ?: return@withContext Result.Error("Kode undangan tidak valid")
            
            val membership = hashMapOf(
                "name" to (orgDoc.getString("name") ?: ""),
                "role" to UserRole.BORROWER.name,
                "joinedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users").document(userId)
                .collection("memberships").document(orgDoc.id).set(membership).await()
                
            // Update activeOrgId in user document
            firestore.collection("users").document(userId).update("activeOrgId", orgDoc.id).await()

            // Also record the member in the organization document
            val orgMember = hashMapOf(
                "name" to (firestore.collection("users").document(userId).get().await().getString("name") ?: ""),
                "role" to UserRole.BORROWER.name,
                "email" to (firestore.collection("users").document(userId).get().await().getString("email") ?: ""),
                "joinedAt" to System.currentTimeMillis()
            )
            firestore.collection("organizations").document(orgDoc.id)
                .collection("members").document(userId).set(orgMember).await()

            val org = Organization(
                id = orgDoc.id,
                name = orgDoc.getString("name") ?: "",
                description = orgDoc.getString("description"),
                role = UserRole.BORROWER
            )
            
            sessionManager.setActiveOrganization(orgDoc.id)
            Result.Success(org)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal bergabung ke organisasi")
        }
    }

    suspend fun refreshOrganizations(): Result<List<Organization>> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.userIdFlow.first() ?: return@withContext Result.Error("User not logged in")
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("memberships")
                .get()
                .await()
                
            val orgs = snapshot.documents.mapNotNull { doc ->
                Organization(
                    id = doc.id,
                    name = doc.getString("name") ?: "Unknown Org",
                    description = doc.getString("description"),
                    role = UserRole.fromString(doc.getString("role") ?: "BORROWER"),
                    inviteCode = doc.getString("inviteCode"),
                    lateFeePerDay = doc.getDouble("lateFeePerDay") ?: 0.0,
                    allowPaidRental = doc.getBoolean("allowPaidRental") ?: false
                )
            }
            Result.Success(orgs)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal memuat organisasi")
        }
    }

    suspend fun getOrganizationDetails(orgId: String): Result<Organization> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("organizations").document(orgId).get().await()
            if (doc.exists()) {
                val userId = sessionManager.userIdFlow.first()
                val role = if (userId != null) {
                    val membershipDoc = firestore.collection("users").document(userId)
                        .collection("memberships").document(orgId).get().await()
                    UserRole.fromString(membershipDoc.getString("role") ?: "BORROWER")
                } else UserRole.BORROWER

                Result.Success(
                    Organization(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown Org",
                        description = doc.getString("description"),
                        role = role,
                        inviteCode = doc.getString("inviteCode"),
                        lateFeePerDay = doc.getDouble("lateFeePerDay") ?: 0.0,
                        allowPaidRental = doc.getBoolean("allowPaidRental") ?: false
                    )
                )
            } else {
                Result.Error("Organisasi tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan saat mengambil data organisasi")
        }
    }

    suspend fun switchOrganization(orgId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sessionManager.setActiveOrganization(orgId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal berpindah organisasi")
        }
    }
}