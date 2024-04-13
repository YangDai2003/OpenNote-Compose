package com.yangdai.opennote.presentation.viewmodel

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.domain.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingScreenViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    fun putInt(key: String, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.putInt(key, value)
        }
    }

    fun getInt(key: String): Int? = runBlocking {
        dataStoreRepository.getInt(key)
    }

    fun putBoolean(key: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.putBoolean(key, value)
        }
    }

    fun getBoolean(key: String): Boolean? = runBlocking {
        dataStoreRepository.getBoolean(key)
    }

    fun getFlow(): Flow<Preferences> {
        return dataStoreRepository.preferencesFlow()
    }

    fun getDataStore() = dataStoreRepository.getDataStore()
}