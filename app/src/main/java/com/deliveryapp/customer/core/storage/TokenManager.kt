package com.deliveryapp.customer.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "customer_session")

class TokenManager(private val context: Context) {
    private object Keys {
        val AccessToken = stringPreferencesKey("access_token")
        val RefreshToken = stringPreferencesKey("refresh_token")
    }

    val accessToken: Flow<String?> = context.sessionDataStore.data.map { it[Keys.AccessToken] }
    val refreshToken: Flow<String?> = context.sessionDataStore.data.map { it[Keys.RefreshToken] }

    suspend fun currentAccessToken(): String? = accessToken.first()
    suspend fun currentRefreshToken(): String? = refreshToken.first()

    suspend fun save(accessToken: String, refreshToken: String) {
        context.sessionDataStore.edit {
            it[Keys.AccessToken] = accessToken
            it[Keys.RefreshToken] = refreshToken
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
