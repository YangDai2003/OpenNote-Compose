package com.yangdai.opennote.ui.event

sealed interface UiEvent {
    data object NavigateBack : UiEvent
}