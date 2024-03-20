package com.yangdai.opennote.note

sealed interface UiEvent {
    data object NavigateBack : UiEvent
}