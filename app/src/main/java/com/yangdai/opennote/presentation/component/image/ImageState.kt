package com.yangdai.opennote.presentation.component.image

sealed interface ImageState {
    data object Loading : ImageState
    data class Success(val imagePath: String, val isPath: Boolean, val isLocalFile: Boolean) :
        ImageState

    data class Error(val message: String) : ImageState
    data object Empty : ImageState
}