package com.yangdai.opennote.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {

    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun getString(key: String): String?
    suspend fun getInt(key: String): Int?
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun putStringSet(key: String, value: Set<String>)
    suspend fun getStringSet(key: String): Set<String>?

    fun intFlow(key: String): Flow<Int>
    fun stringFlow(key: String): Flow<String>
    fun booleanFlow(key: String): Flow<Boolean>
    fun stringSetFlow(key: String): Flow<Set<String>>
    fun preferencesFlow(): Flow<Preferences>
    fun getDataStore(): DataStore<Preferences>
}