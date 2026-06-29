package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.Transaction
import com.example.simple.domain.model.TransactionStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BorrowRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager,
) {
    suspend fun submitBorrowRequest(
        orgId: String,
        itemId: String,
        quantity: Int,
        startDate: Long,
        endDate: Long,
        notes: String?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.userIdFlow.first() ?: return@withContext Result.Error("User not logged in")
            
            // Get user info and item info for better dashboard display
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userName = userDoc.getString("name") ?: "Unknown User"
            
            val itemDoc = firestore.collection("organizations").document(orgId)
                .collection("items").document(itemId).get().await()
            val itemName = itemDoc.getString("name") ?: "Unknown Item"
            val itemEmoji = itemDoc.getString("emoji") ?: "📦"
            val totalStock = itemDoc.getLong("totalStock")?.toInt() ?: 0
            val rentalPrice = itemDoc.getDouble("rentalPrice") ?: 0.0
            val isPaidRental = itemDoc.getBoolean("isPaidRental") ?: false

            // Booking Logic: Check for overlapping transactions for this item
            val overlappingSnapshot = firestore.collectionGroup("transactions")
                .whereEqualTo("itemId", itemId)
                .whereIn("status", listOf(TransactionStatus.APPROVED.name, TransactionStatus.BORROWED.name))
                .get()
                .await()
            
            val borrowedDuringPeriod = overlappingSnapshot.documents.mapNotNull { doc ->
                val bDate = doc.getLong("borrowDate") ?: 0L
                val dDate = doc.getLong("dueDate") ?: 0L
                val qty = doc.getLong("quantity")?.toInt() ?: 0
                
                // Overlap check: (StartA <= EndB) and (EndA >= StartB)
                if (startDate <= dDate && endDate >= bDate) qty else null
            }.sum()

            if (totalStock - borrowedDuringPeriod < quantity) {
                return@withContext Result.Error("Stok tidak mencukupi untuk periode tersebut (Tersedia: ${totalStock - borrowedDuringPeriod})")
            }

            // Calculate Fee
            val durationMillis = (endDate - startDate).coerceAtLeast(0L)
            val durationDays = (durationMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            val totalFee = if (isPaidRental) rentalPrice * quantity * durationDays else 0.0

            val request = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "requesterOrgId" to orgId, // The org that is currently active and borrowing
                "itemId" to itemId,
                "itemName" to itemName,
                "itemEmoji" to itemEmoji,
                "quantity" to quantity,
                "borrowDate" to startDate,
                "dueDate" to endDate,
                "notes" to notes,
                "status" to TransactionStatus.PENDING.name,
                "totalFee" to totalFee,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("organizations")
                .document(orgId)
                .collection("transactions")
                .add(request)
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal mengajukan peminjaman")
        }
    }
}

@Singleton
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observeTransactions(orgId: String, status: String? = null): Flow<List<Transaction>> = callbackFlow {
        // Query transactions where this organization is either the owner OR the requester
        // Since Firestore doesn't support logical OR across different fields easily in one query without collectionGroup
        // and we are already using collectionGroup for items, let's use collectionGroup for transactions too
        // and filter by the organizationId field (owner) or a new requesterOrganizationId field.
        
        // Alternative: Just query the transactions collection under the specific organization
        // The current structure is organizations/{orgId}/transactions
        // This means it only shows transactions FOR items owned by that organization.
        
        // To show "What I borrowed from others", we need a global search or a user-specific subcollection.
        // Let's use collectionGroup("transactions") to find all transactions related to this organization
        
        val listener = firestore.collectionGroup("transactions")
            .whereIn("status", status?.let { listOf(it) } ?: TransactionStatus.entries.map { it.name })
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val txs = snapshot?.documents?.mapNotNull { doc ->
                    val requesterOrgId = doc.getString("requesterOrgId") ?: ""
                    
                    // Filter: ONLY include if this organization is the one that MADE the request
                    if (requesterOrgId != orgId) return@mapNotNull null

                    val itemOrgId = doc.reference.parent.parent?.id ?: ""

                    Transaction(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName"),
                        itemId = doc.getString("itemId") ?: "",
                        itemName = doc.getString("itemName") ?: "Unknown Item",
                        itemEmoji = doc.getString("itemEmoji") ?: "📦",
                        organizationId = itemOrgId,
                        organizationName = doc.getString("organizationName"),
                        quantity = doc.getLong("quantity")?.toInt() ?: 0,
                        borrowDate = doc.getLong("borrowDate") ?: 0L,
                        dueDate = doc.getLong("dueDate") ?: 0L,
                        returnDate = doc.getLong("returnDate"),
                        status = TransactionStatus.fromString(doc.getString("status") ?: "PENDING"),
                        notes = doc.getString("notes"),
                        approvedBy = doc.getString("approvedBy"),
                        conditionBefore = ItemCondition.fromString(doc.getString("conditionBefore") ?: "GOOD"),
                        conditionAfter = doc.getString("conditionAfter")?.let { ItemCondition.fromString(it) },
                        totalFee = doc.getDouble("totalFee") ?: 0.0,
                        lateFee = doc.getDouble("lateFee") ?: 0.0
                    )
                }?.sortedByDescending { it.borrowDate } ?: emptyList()

                trySend(txs)
            }

        awaitClose { listener.remove() }
    }

    suspend fun refreshTransactions(orgId: String, status: String? = null): Result<Unit> = Result.Success(Unit)

    suspend fun returnItem(orgId: String, transactionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val requestRef = firestore.collection("organizations")
                    .document(orgId)
                    .collection("transactions")
                    .document(transactionId)
                
                val requestDoc = transaction.get(requestRef)
                val itemId = requestDoc.getString("itemId") ?: throw Exception("Item ID tidak ditemukan")
                val quantity = requestDoc.getLong("quantity")?.toInt() ?: 0
                
                val itemRef = firestore.collection("organizations")
                    .document(orgId)
                    .collection("items")
                    .document(itemId)
                
                val itemDoc = transaction.get(itemRef)
                val availableStock = itemDoc.getLong("availableStock")?.toInt() ?: 0
                val totalStock = itemDoc.getLong("totalStock")?.toInt() ?: 0
                
                // Update transaction status
                transaction.update(
                    requestRef,
                    "status", TransactionStatus.RETURNED.name,
                    "returnDate", System.currentTimeMillis(),
                    "updatedAt", System.currentTimeMillis()
                )
                
                // Increment item stock, but don't exceed totalStock (safety check)
                val newStock = (availableStock + quantity).coerceAtMost(totalStock)
                transaction.update(itemRef, "availableStock", newStock)
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal mencatat pengembalian")
        }
    }
}