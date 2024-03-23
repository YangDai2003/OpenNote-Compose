package com.yangdai.opennote.ui.viewmodel

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.Constants.APP_COLOR
import com.yangdai.opennote.Constants.APP_THEME
import com.yangdai.opennote.Constants.NEED_PASSWORD
import com.yangdai.opennote.ui.state.SettingsState
import com.yangdai.opennote.domain.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaseScreenViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    //isLoading 状态，初始值为 true
    private  val _isLoading = MutableStateFlow( true )
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            //延迟模拟一些后台处理，如获取数据
            delay( 500 )
            //任务完成后将 isLoading 设置为 false 以隐藏启动屏幕
            _isLoading.value = false
        }
    }

    val stateFlow: StateFlow<SettingsState> = getData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsState()
        )

    private fun getData(): Flow<SettingsState> = flow {
        combine(
            dataStoreRepository.intFlow(APP_THEME),
            dataStoreRepository.intFlow(APP_COLOR),
            dataStoreRepository.booleanFlow(NEED_PASSWORD),
        ) { mode, color, needPassword ->
            SettingsState(mode = mode, color = color, needPassword = needPassword)
        }.collect {
            emit(it)
        }
    }.flowOn(Dispatchers.IO)

    fun getFlow(): Flow<Preferences> {
        return dataStoreRepository.preferencesFlow()
    }

    fun putInt(key: String, value: Int) {
        viewModelScope.launch {
            dataStoreRepository.putInt(key, value)
        }
    }
    fun putBoolean(key: String, value: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.putBoolean(key, value)
        }
    }
}