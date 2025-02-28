package com.yangdai.opennote.domain.repository

import kotlinx.coroutines.flow.Flow

interface WidgetDataStoreRepository {

    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putFloat(key: String, value: Float)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putStringSet(key: String, value: Set<String>)

    fun intFlow(key: String): Flow<Int>
    fun floatFlow(key: String): Flow<Float>
    fun stringFlow(key: String): Flow<String>
    fun booleanFlow(key: String): Flow<Boolean>
    fun stringSetFlow(key: String): Flow<Set<String>>

    fun getStringValue(key: String, defaultValue: String): String
    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean
}