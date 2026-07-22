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
    }

    val token: Flow<String?> = dataStore.data.map { it[Keys.TOKEN] }
    val user: Flow<User?> = dataStore.data.map { it[Keys.USER]?.let { raw -> runCatching { json.decodeFromString<User>(raw) }.getOrNull() } }

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
