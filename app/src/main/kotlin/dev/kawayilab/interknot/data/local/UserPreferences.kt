package dev.kawayilab.interknot.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val token: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
        }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
    }
}
