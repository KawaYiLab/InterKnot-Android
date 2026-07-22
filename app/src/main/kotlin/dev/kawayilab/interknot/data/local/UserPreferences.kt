package dev.kawayilab.interknot.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
    }

    val token: Flow<String?> = dataStore.data.map { it[Keys.TOKEN] }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[Keys.TOKEN] = token }
    }

    suspend fun clearToken() {
        dataStore.edit { it.remove(Keys.TOKEN) }
    }
}
