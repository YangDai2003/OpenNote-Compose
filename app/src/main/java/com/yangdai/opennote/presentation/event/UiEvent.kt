package com.yangdai.opennote.presentation.event

sealed interface UiEvent {
    data object NavigateBack : UiEvent
}