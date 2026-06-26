package com.example.simple.data.repository

import com.example.simple.common.Result
import com.example.simple.data.local.preferences.SessionManager
import com.example.simple.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager,
) {
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return@withContext Result.Error("Login gagal")
            
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (userDoc.exists()) {
                val name = userDoc.getString("name") ?: ""
                var activeOrgId = userDoc.getString("activeOrgId")
                
                // If no activeOrgId in user doc, check if they have any memberships
                if (activeOrgId == null) {
                    val memberships = firestore.collection("users").document(firebaseUser.uid)
                        .collection("memberships").limit(1).get().await()
                    if (!memberships.isEmpty) {
                        activeOrgId = memberships.documents.first().id
                        // Update user doc with this activeOrgId for future logins
                        firestore.collection("users").document(firebaseUser.uid)
                            .update("activeOrgId", activeOrgId)
                    }
                }
                
                sessionManager.saveSession(firebaseUser.uid, firebaseUser.uid)
                activeOrgId?.let { sessionManager.setActiveOrganization(it) }
                
                Result.Success(
                    User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email,
                        activeOrgId = activeOrgId
                    )
                )
            } else {
                Result.Error("Data pengguna tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan saat login")
        }
    }

    suspend fun signup(name: String, email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: return@withContext Result.Error("Pendaftaran gagal")
                
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                
                firestore.collection("users").document(firebaseUser.uid).set(userData).await()
                
                sessionManager.saveSession(firebaseUser.uid, firebaseUser.uid)
                Result.Success(
                    User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email
                    )
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("already in use") == true -> "Email sudah terdaftar. Silakan gunakan email lain atau login."
                    else -> e.message ?: "Terjadi kesalahan saat pendaftaran"
                }
                Result.Error(errorMessage)
            }
        }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            sessionManager.clearSession()
            Result.Success(Unit)
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.Success(Unit)
        }
    }

    suspend fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal mengirim email reset password")
        }
    }

    suspend fun updateProfile(name: String, phone: String?, avatar: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.Error("User not logged in")
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "updatedAt" to System.currentTimeMillis()
            )
            phone?.let { updates["phone"] = it }
            avatar?.let { updates["avatar"] = it }

            firestore.collection("users").document(userId).update(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal memperbarui profil")
        }
    }
}