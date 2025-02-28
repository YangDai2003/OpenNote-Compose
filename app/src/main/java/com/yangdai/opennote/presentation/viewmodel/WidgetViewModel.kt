package com.yangdai.opennote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yangdai.opennote.domain.repository.WidgetDataStoreRepository
import com.yangdai.opennote.presentation.state.WidgetBackgroundColor
import com.yangdai.opennote.presentation.state.WidgetState
import com.yangdai.opennote.presentation.state.WidgetTextSize
import com.yangdai.opennote.presentation.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val widgetDataStoreRepository: WidgetDataStoreRepository,
) : ViewModel() {

    val widgetSettingsState: StateFlow<WidgetState> = combine(
        widgetDataStoreRepository.intFlow(Constants.Widget.WIDGET_TEXT_SIZE),
        widgetDataStoreRepository.intFlow(Constants.Widget.WIDGET_TEXT_LINES),
        widgetDataStoreRepository.intFlow(Constants.Widget.WIDGET_BACKGROUND_COLOR)
    ) { v1, v2, v3 ->
        WidgetState(
            textSize = WidgetTextSize.fromInt(v1),
            textLines = v2,
            backgroundColor = WidgetBackgroundColor.fromInt(v3)
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = WidgetState()
    )

    fun <T> putPreferenceValue(key: String, value: T) {
        viewModelScope.launch(Dispatchers.IO) {
            when (value) {
                is Int -> widgetDataStoreRepository.putInt(key, value)
                is Float -> widgetDataStoreRepository.putFloat(key, value)
                is Boolean -> widgetDataStoreRepository.putBoolean(key, value)
                is String -> widgetDataStoreRepository.putString(key, value)
                is Set<*> -> widgetDataStoreRepository.putStringSet(
                    key, value.filterIsInstance<String>().toSet()
                )

                else -> throw IllegalArgumentException("Unsupported value type")
            }
        }
    }
}