package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.room.dao.ItemDao
import com.example.simple.data.local.room.entity.ItemEntity
import com.example.simple.domain.model.Item
import com.example.simple.domain.model.ItemCondition
import com.example.simple.domain.model.ItemStatus
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val itemDao: ItemDao,
) {
    /** Observe items from Local Database with Firestore Sync. */
    fun observeItems(orgId: String, search: String = "", category: String? = null, forceRefresh: Boolean = false): Flow<List<Item>> {
        android.util.Log.d("ItemRepo", "Observing items for org: $orgId")
        return itemDao.getItemsByOrgId(orgId).map { entities ->
            android.util.Log.d("ItemRepo", "Local DB returned ${entities.size} items")
            entities.map { it.toDomain() }
                .filter { item ->
                    (search.isEmpty() || item.name.contains(search, ignoreCase = true)) &&
                    (category == null || item.category == category)
                }
        }.onStart {
            refreshItems(orgId)
        }
    }

    suspend fun refreshItems(orgId: String): Result<Unit> = try {
        val snapshot = firestore.collection("organizations")
            .document(orgId)
            .collection("items")
            .get(Source.SERVER)
            .await()

            val items = snapshot.documents.mapNotNull { doc ->
                val imageUrl = doc.getString("imageUrl")
                android.util.Log.d("ItemRepo", "Item: ${doc.getString("name")}, Image: $imageUrl")
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
                    isPaidRental = doc.getBoolean("isPaidRental") ?: false,
                    imageUrl = imageUrl
                )
            }

        itemDao.syncItems(orgId, items.map { ItemEntity.fromDomain(it) })
        Result.Success(Unit)
    } catch (e: Exception) {
        android.util.Log.e("ItemRepo", "Refresh Error: ${e.message}", e)
        Result.Error(e.message ?: "Gagal refresh data")
    }

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
                        isPaidRental = doc.getBoolean("isPaidRental") ?: false,
                        imageUrl = doc.getString("imageUrl")
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
                    isPaidRental = doc.getBoolean("isPaidRental") ?: false,
                    imageUrl = doc.getString("imageUrl")
                )
            )
        } else {
            Result.Error("Barang tidak ditemukan")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Terjadi kesalahan saat mengambil detail barang")
    }
    suspend fun deleteItem(orgId: String, itemId: String): Result<Unit> = try {
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
