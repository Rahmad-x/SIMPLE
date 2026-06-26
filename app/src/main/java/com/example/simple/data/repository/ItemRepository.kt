package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    /** Observe items from Firestore. */
    fun observeItems(orgId: String, search: String = "", category: String? = null, forceRefresh: Boolean = false): Flow<List<Item>> = callbackFlow {
        val collection = firestore.collection("organizations")
            .document(orgId)
            .collection("items")
        
        var query: Query = collection

        if (category != null) {
            query = query.whereEqualTo("category", category)
        }

        // If forceRefresh is requested, we do a one-time get from server to populate/update cache
        if (forceRefresh) {
            try {
                collection.get(Source.SERVER).await()
            } catch (e: Exception) {
                // Ignore error, fallback to normal listener
            }
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                val name = doc.getString("name") ?: ""
                if (search.isNotEmpty() && !name.contains(search, ignoreCase = true)) return@mapNotNull null
                
                Item(
                    id = doc.id,
                    organizationId = orgId,
                    name = name,
                    description = doc.getString("description"),
                    category = doc.getString("category") ?: "Uncategorized",
                    location = doc.getString("location") ?: "Unknown",
                    totalStock = doc.getLong("totalStock")?.toInt() ?: 0,
                    availableStock = doc.getLong("availableStock")?.toInt() ?: 0,
                    condition = ItemCondition.fromString(doc.getString("condition") ?: "GOOD"),
                    emoji = doc.getString("emoji") ?: "📦",
                    status = ItemStatus.fromString(doc.getString("status") ?: "AVAILABLE"),
                    rentalPrice = doc.getDouble("rentalPrice") ?: 0.0,
                    isPaidRental = doc.getBoolean("isPaidRental") ?: false
                )
            } ?: emptyList()

            trySend(items)
        }

        awaitClose { listener.remove() }
    }

    /** Force refresh by fetching from server. */
    suspend fun refreshItems(orgId: String): Result<Unit> = try {
        firestore.collection("organizations")
            .document(orgId)
            .collection("items")
            .get(Source.SERVER) // Force fetch from server
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Gagal refresh data")
    }

    /** Search items across all organizations using Collection Group. */
    fun searchGlobalItems(search: String): Flow<List<Item>> = callbackFlow {
        val listener = firestore.collectionGroup("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: ""
                    if (search.isNotEmpty() && !name.contains(search, ignoreCase = true)) return@mapNotNull null
                    
                    // We need to get the orgId from the parent document path
                    // Path is: organizations/{orgId}/items/{itemId}
                    val orgId = doc.reference.parent.parent?.id ?: ""

                    Item(
                        id = doc.id,
                        organizationId = orgId,
                        name = name,
                        description = doc.getString("description"),
                        category = doc.getString("category") ?: "Uncategorized",
                        location = doc.getString("location") ?: "Unknown",
                        totalStock = doc.getLong("totalStock")?.toInt() ?: 0,
                        availableStock = doc.getLong("availableStock")?.toInt() ?: 0,
                        condition = ItemCondition.fromString(doc.getString("condition") ?: "GOOD"),
                        emoji = doc.getString("emoji") ?: "📦",
                        status = ItemStatus.fromString(doc.getString("status") ?: "AVAILABLE"),
                        rentalPrice = doc.getDouble("rentalPrice") ?: 0.0,
                        isPaidRental = doc.getBoolean("isPaidRental") ?: false
                    )
                } ?: emptyList()

                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getItemDetail(orgId: String, itemId: String): Result<Item> = try {
        val doc = firestore.collection("organizations")
            .document(orgId)
            .collection("items")
            .document(itemId)
            .get()
            .await()

        if (doc.exists()) {
            Result.Success(
                Item(
                    id = doc.id,
                    organizationId = orgId,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description"),
                    category = doc.getString("category") ?: "Uncategorized",
                    location = doc.getString("location") ?: "Unknown",
                    totalStock = doc.getLong("totalStock")?.toInt() ?: 0,
                    availableStock = doc.getLong("availableStock")?.toInt() ?: 0,
                    condition = ItemCondition.fromString(doc.getString("condition") ?: "GOOD"),
                    emoji = doc.getString("emoji") ?: "📦",
                    status = ItemStatus.fromString(doc.getString("status") ?: "AVAILABLE"),
                    rentalPrice = doc.getDouble("rentalPrice") ?: 0.0,
                    isPaidRental = doc.getBoolean("isPaidRental") ?: false
                )
            )
        } else {
            Result.Error("Barang tidak ditemukan")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Terjadi kesalahan saat mengambil detail barang")
    }
}
