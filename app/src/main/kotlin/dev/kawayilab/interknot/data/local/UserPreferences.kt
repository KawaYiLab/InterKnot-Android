package dev.kawayilab.interknot.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kawayilab.interknot.data.api.TokenManager
import dev.kawayilab.interknot.model.User
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER = stringPreferencesKey("user")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
        val DRAFT = stringPreferencesKey("create_draft")
    }

    val token: Flow<String?> = dataStore.data.map { it[Keys.TOKEN] }
    val user: Flow<User?> = dataStore.data.map { it[Keys.USER]?.let { raw -> runCatching { json.decodeFromString<User>(raw) }.getOrNull() } }

    val searchHistory: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[Keys.SEARCH_HISTORY]?.let { raw ->
            runCatching { json.decodeFromString<List<String>>(raw) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun addSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { prefs ->
            val current = prefs[Keys.SEARCH_HISTORY]?.let { raw ->
                runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = (listOf(trimmed) + current.filter { it != trimmed }).take(20)
            prefs[Keys.SEARCH_HISTORY] = json.encodeToString(updated)
        }
    }

    suspend fun clearSearchHistory() {
        dataStore.edit { it.remove(Keys.SEARCH_HISTORY) }
    }

    val createDraft: Flow<CreateDraft?> = dataStore.data.map { prefs ->
        prefs[Keys.DRAFT]?.let { raw ->
            runCatching { json.decodeFromString<CreateDraft>(raw) }.getOrNull()
        }
    }

    suspend fun saveCreateDraft(draft: CreateDraft) {
        dataStore.edit { it[Keys.DRAFT] = json.encodeToString(draft) }
    }

    suspend fun clearCreateDraft() {
        dataStore.edit { it.remove(Keys.DRAFT) }
    }

    suspend fun saveSession(token: String, user: User) {
        dataStore.edit {
            it[Keys.TOKEN] = token
            it[Keys.USER] = json.encodeToString(user)
        }
        TokenManager.token = token
    }

    suspend fun clearSession() {
        dataStore.edit {
            it.remove(Keys.TOKEN)
            it.remove(Keys.USER)
        }
        TokenManager.token = null
    }
}
