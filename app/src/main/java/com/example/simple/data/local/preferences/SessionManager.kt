package com.example.simple.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "simple_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val ACTIVE_ORG_ID = stringPreferencesKey("active_org_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val activeOrgIdFlow: Flow<String?> = context.dataStore.data.map { it[Keys.ACTIVE_ORG_ID] }
    val userIdFlow: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ID] }

    suspend fun saveSession(token: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = userId
        }
    }

    suspend fun setActiveOrganization(orgId: String) {
        context.dataStore.edit { prefs -> prefs[Keys.ACTIVE_ORG_ID] = orgId }
    }

    suspend fun getToken(): String = context.dataStore.data.first()[Keys.TOKEN] ?: ""

    suspend fun getActiveOrgId(): String? = context.dataStore.data.first()[Keys.ACTIVE_ORG_ID]

    suspend fun isLoggedIn(): Boolean = getToken().isNotEmpty()

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}