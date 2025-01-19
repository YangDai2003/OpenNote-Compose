package com.yangdai.opennote.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yangdai.opennote.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private const val PREFERENCES_NAME = "settings_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class DataStoreRepositoryImpl @Inject constructor(
    private val context: Context
) : DataStoreRepository {

    override suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putFloat(key: String, value: Float) {
        val preferencesKey = floatPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putStringSet(key: String, value: Set<String>) {
        val preferencesKey = stringSetPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override fun intFlow(key: String): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: 0
        }
    }

    override fun floatFlow(key: String): Flow<Float> {
        val preferencesKey = floatPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: 0f
        }
    }

    override fun stringFlow(key: String): Flow<String> {
        val preferencesKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: ""
        }
    }

    override fun booleanFlow(key: String): Flow<Boolean> {
        val preferencesKey = booleanPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] == true
        }
    }

    override fun stringSetFlow(key: String): Flow<Set<String>> {
        val preferencesKey = stringSetPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferencesKey] ?: setOf()
        }
    }
}
