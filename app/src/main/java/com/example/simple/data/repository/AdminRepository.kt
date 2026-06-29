package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.remote.api.FakeStoreApiService
import com.example.simple.domain.model.BorrowRequest
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus
import com.example.simple.domain.model.Organization
import com.example.simple.domain.model.TransactionStatus
import com.example.simple.domain.model.User
import com.example.simple.domain.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fakeStoreApiService: FakeStoreApiService,
) {
    suspend fun getExternalProducts(orgId: String): Result<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val products = fakeStoreApiService.getProducts()
            val items = products.map { 
                Item(
                    id = UUID.randomUUID().toString(),
                    organizationId = orgId,
                    name = it.title,
                    description = it.description,
                    category = it.category,
                    location = "Gudang",
                    totalStock = 5,
                    availableStock = 5,
                    condition = ItemCondition.GOOD,
                    emoji = "🛒",
                    status = ItemStatus.AVAILABLE,
                    rentalPrice = it.price,
                    isPaidRental = true
                )
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal memuat produk eksternal")
        }
    }
    fun observePendingRequests(orgId: String): Flow<List<BorrowRequest>> = callbackFlow {
        val listener = firestore.collection("organizations")
            .document(orgId)
            .collection("transactions")
            .whereEqualTo("status", TransactionStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    BorrowRequest(
                        id = doc.id,
                        itemId = doc.getString("itemId") ?: "",
                        itemName = doc.getString("itemName") ?: "Unknown",
                        requesterId = doc.getString("userId") ?: "",
                        requesterName = doc.getString("userName") ?: "Unknown User",
                        organizationId = orgId,
                        quantity = doc.getLong("quantity")?.toInt() ?: 0,
                        startDate = doc.getLong("borrowDate") ?: 0L,
                        endDate = doc.getLong("dueDate") ?: 0L,
                        notes = doc.getString("notes"),
                        status = TransactionStatus.PENDING
                    )
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveRequest(orgId: String, requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val requestRef = firestore.collection("organizations")
                    .document(orgId)
                    .collection("transactions")
                    .document(requestId)
                
                val requestDoc = transaction.get(requestRef)
                val itemId = requestDoc.getString("itemId") ?: throw Exception("Item ID tidak ditemukan")
                val quantity = requestDoc.getLong("quantity")?.toInt() ?: 0
                
                val itemRef = firestore.collection("organizations")
                    .document(orgId)
                    .collection("items")
                    .document(itemId)
                
                val itemDoc = transaction.get(itemRef)
                val availableStock = itemDoc.getLong("availableStock")?.toInt() ?: 0
                
                if (availableStock < quantity) {
                    throw Exception("Stok tidak mencukupi untuk disetujui (Tersedia: $availableStock)")
                }
                
                // Update transaction status
                transaction.update(requestRef, "status", TransactionStatus.APPROVED.name, "updatedAt", System.currentTimeMillis())
                
                // Decrement item stock
                transaction.update(itemRef, "availableStock", availableStock - quantity)
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal menyetujui permintaan")
        }
    }

    suspend fun rejectRequest(orgId: String, requestId: String, reason: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("organizations")
                    .document(orgId)
                    .collection("transactions")
                    .document(requestId)
                    .update(
                        "status", TransactionStatus.REJECTED.name,
                        "rejectionReason", reason,
                        "updatedAt", System.currentTimeMillis()
                    ).await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal menolak permintaan")
            }
        }

    suspend fun addItem(orgId: String, item: Item): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = hashMapOf(
                "name" to item.name,
                "description" to item.description,
                "category" to item.category,
                "location" to item.location,
                "totalStock" to item.totalStock,
                "availableStock" to item.availableStock,
                "condition" to item.condition.name,
                "emoji" to item.emoji,
                "status" to item.status.name,
                "rentalPrice" to item.rentalPrice,
                "isPaidRental" to item.isPaidRental,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("organizations")
                .document(orgId)
                .collection("items")
                .add(data)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal menambah barang")
        }
    }

    suspend fun updateItem(orgId: String, item: Item): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "name" to item.name,
                    "description" to item.description,
                    "category" to item.category,
                    "location" to item.location,
                    "totalStock" to item.totalStock,
                    "availableStock" to item.availableStock,
                    "condition" to item.condition.name,
                    "emoji" to item.emoji,
                    "status" to item.status.name,
                    "rentalPrice" to item.rentalPrice,
                    "isPaidRental" to item.isPaidRental,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("organizations")
                    .document(orgId)
                    .collection("items")
                    .document(item.id)
                    .set(data)
                    .await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Gagal memperbarui barang")
            }
        }

    suspend fun deleteItem(orgId: String, itemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("organizations")
                .document(orgId)
                .collection("items")
                .document(itemId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal menghapus barang")
        }
    }

    suspend fun getMembers(orgId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("organizations")
                .document(orgId)
                .collection("members")
                .get()
                .await()
            
            val members = snapshot.documents.map { doc ->
                User(
                    id = doc.id,
                    name = doc.getString("name") ?: "Unknown",
                    email = doc.getString("email") ?: "",
                    organizations = listOf(
                        Organization(
                            id = orgId,
                            name = "", // Not needed for this list
                            role = UserRole.fromString(doc.getString("role") ?: "BORROWER")
                        )
                    )
                )
            }
            Result.Success(members)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal memuat anggota")
        }
    }

    suspend fun updateMemberRole(orgId: String, userId: String, newRole: UserRole): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batch = firestore.batch()
            
            // Update in organization's member list
            val orgMemberRef = firestore.collection("organizations").document(orgId)
                .collection("members").document(userId)
            batch.update(orgMemberRef, "role", newRole.name)
            
            // Update in user's memberships list
            val userMembershipRef = firestore.collection("users").document(userId)
                .collection("memberships").document(orgId)
            batch.update(userMembershipRef, "role", newRole.name)
            
            batch.commit().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal mengubah peran anggota")
        }
    }
}
