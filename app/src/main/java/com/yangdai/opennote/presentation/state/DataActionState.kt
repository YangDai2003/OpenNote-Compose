package com.yangdai.opennote.presentation.state

data class DataActionState(
    val loading: Boolean = false,
    val progress: Float = 0f,
    val infinite: Boolean = false,
    val error: String = ""
)